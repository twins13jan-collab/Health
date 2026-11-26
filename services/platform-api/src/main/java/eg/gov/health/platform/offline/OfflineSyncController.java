package eg.gov.health.platform.offline;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.*; import java.util.*;
/** Offline work & secure sync (cycle 237). Devices are trusted or
 *  revoked; work happens inside a bounded, expiring package; sync is
 *  idempotent — a replayed envelope is a duplicate, never a second
 *  application; stale reference data halts fresh-verification
 *  operations; clinical/financial conflicts go to the facility manager
 *  under a domain rule — never automatic last-write-wins. */
@RestController @RequestMapping("/api/offline")
public class OfflineSyncController {
  private final DeviceTrustRepository devices; private final OfflineWorkPackageRepository packages;
  private final LocalTransactionRepository transactions; private final SyncEnvelopeRepository envelopes;
  private final ConflictCaseRepository conflicts; private final AuditService audit;
  public OfflineSyncController(DeviceTrustRepository devices, OfflineWorkPackageRepository packages,
                               LocalTransactionRepository transactions, SyncEnvelopeRepository envelopes,
                               ConflictCaseRepository conflicts, AuditService audit) {
    this.devices=devices; this.packages=packages; this.transactions=transactions;
    this.envelopes=envelopes; this.conflicts=conflicts; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private DeviceTrust device(UUID id) {
    return devices.findById(id).orElseThrow(() -> BizException.notFound("device_not_found","الجهاز غير موجود"));
  }
  private OfflineWorkPackage pkg(UUID id) {
    return packages.findById(id).orElseThrow(() -> BizException.notFound("package_not_found","حزمة العمل غير موجودة"));
  }

  public record DeviceRequest(String deviceCode, String userRef, Boolean encryptionEnabled) {}
  @PostMapping("/devices")
  Map<String,Object> device(@RequestBody DeviceRequest r) {
    var p = principal();
    if (r.deviceCode()==null) throw BizException.badRequest("device_code_required","معرف الجهاز مطلوب");
    var d = devices.save(new DeviceTrust(UUID.fromString(p.facilityId()), p.governorate(), r.deviceCode(), r.userRef(),
      Boolean.TRUE.equals(r.encryptionEnabled())));
    audit.record("offline.device.registered","device_trust",d.getId().toString(),"{}");
    return Map.of("ok",true,"device",d);
  }
  @PostMapping("/devices/{id}/revoke")
  Map<String,Object> revoke(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var d = device(id);
    // Security revokes a lost device; its packages die with it.
    d.revoke(p_email(), body.get("reason")); devices.save(d);
    for (var pk : packages.findByDeviceIdOrderByCreatedAtDesc(id)) {
      if ("active".equals(pk.getStatus())) { pk.setStatus("revoked"); packages.save(pk); }
    }
    audit.record("offline.device.revoked","device_trust",id.toString(),"{}");
    return Map.of("ok",true,"device",d,"institutional_data","wipe on next contact","packages_revoked",true);
  }
  private String p_email() { var p = TenantContext.get(); return p==null?null:p.email(); }

  public record PackageRequest(UUID deviceId, String userRef, String scope, String allowedData, Integer referenceDataVersion, OffsetDateTime validUntil) {}
  @PostMapping("/packages")
  Map<String,Object> pkg(@RequestBody PackageRequest r) {
    var p = principal();
    if (r.deviceId()==null||r.validUntil()==null) throw BizException.badRequest("invalid_package","الجهاز ومدة الصلاحية مطلوبان — العمل المحلي داخل حزمة محددة النطاق والمدة");
    var d = device(r.deviceId());
    if (!"trusted".equals(d.getStatus())) throw BizException.conflict("device_not_trusted","لا حزمة لجهاز مبطل الاعتماد");
    var pk = packages.save(new OfflineWorkPackage(d.getFacilityId(), p.governorate(), r.deviceId(), r.userRef(),
      r.scope(), r.allowedData(), r.referenceDataVersion()==null?1:r.referenceDataVersion(), r.validUntil()));
    audit.record("offline.package.issued","offline_work_package",pk.getId().toString(),"{}");
    return Map.of("ok",true,"package",pk,"note","local privilege never exceeds central privilege");
  }

  public record TxDto(String localId, String operationType, String domain, String payloadHash,
                      OffsetDateTime clientTime, Integer referenceDataVersion, Boolean conflictsWithServer,
                      String localSummary, String serverSummary) {}
  public record SyncRequest(String syncId, String contentHash, List<TxDto> transactions) {}
  @PostMapping("/packages/{id}/sync")
  Map<String,Object> sync(@PathVariable UUID id, @RequestBody SyncRequest r) {
    var p = principal(); var pk = pkg(id); var dev = device(pk.getDeviceId());
    if (r.syncId()==null) throw BizException.badRequest("sync_id_required","معرف المزامنة مطلوب لضمان عدم التكرار");
    // Idempotency first: a replayed envelope is a duplicate, never re-applied.
    var prior = envelopes.findByPackageIdAndSyncId(id, r.syncId()).orElse(null);
    if (prior != null) {
      return Map.of("ok",true,"duplicate",true,"envelope",prior,
        "note","sync replay detected — no transaction was applied twice");
    }
    if (!OfflineRules.packageUsable(pk.getStatus(), pk.getValidUntil(), OffsetDateTime.now(), dev.getStatus(), dev.isEncryptionEnabled())) {
      var env = envelopes.save(new SyncEnvelope(pk.getFacilityId(), p.governorate(), id, r.syncId(), r.contentHash(), 0));
      env.finish("rejected", 0); envelopes.save(env);
      throw BizException.conflict("package_not_usable","الحزمة منتهية أو مبطلة أو الجهاز غير موثوق/غير مشفر — المزامنة مرفوضة");
    }
    var txs = r.transactions()==null ? List.<TxDto>of() : r.transactions();
    var env = envelopes.save(new SyncEnvelope(pk.getFacilityId(), p.governorate(), id, r.syncId(), r.contentHash(), txs.size()));
    int applied = 0, conflicted = 0, rejected = 0, duplicates = 0;
    long seq = System.currentTimeMillis();
    for (var t : txs) {
      if (t.localId()==null||t.operationType()==null) { rejected++; continue; }
      var existing = transactions.findByPackageIdAndLocalId(id, t.localId()).orElse(null);
      if (existing != null) { duplicates++; continue; } // same local transaction replayed inside a new envelope
      var tx = new LocalTransaction(pk.getFacilityId(), p.governorate(), id, t.localId(), t.operationType(),
        t.domain(), t.payloadHash(), t.clientTime(), t.referenceDataVersion()==null?1:t.referenceDataVersion(),
        Boolean.TRUE.equals(t.conflictsWithServer()));
      if (!OfflineRules.withinScope(pk.getScope(), t.operationType())) {
        tx.setStatus("rejected"); transactions.save(tx); rejected++; continue;
      }
      if (!OfflineRules.freshEnough(t.operationType(), tx.getReferenceDataVersion())) {
        tx.setStatus("rejected"); transactions.save(tx); rejected++; continue;
      }
      if (tx.isConflictsWithServer()) {
        tx.setStatus("conflicted"); transactions.save(tx);
        var cc = new ConflictCase(pk.getFacilityId(), p.governorate(), tx.getId(), tx.getDomain(), t.localSummary(), t.serverSummary());
        if (OfflineRules.autoResolvable(tx.getDomain())) {
          cc.resolve("administrative: server-wins with local copy retained", "system", "auto-resolved by domain rule");
          tx.setStatus("synced"); tx.setServerSeq(seq++); transactions.save(tx); applied++;
        } else {
          conflicted++; // clinical/financial: waits for the facility manager — never last-write-wins
        }
        conflicts.save(cc);
        continue;
      }
      tx.setStatus("synced"); tx.setServerSeq(seq++); transactions.save(tx); applied++;
    }
    env.finish("applied", applied); envelopes.save(env);
    audit.record("offline.sync.applied","sync_envelope",env.getId().toString(),"{}");
    return Map.of("ok",true,"envelope",env,"applied",applied,"conflicted",conflicted,
      "rejected",rejected,"duplicates_skipped",duplicates,
      "unconfirmed_operations_visible",true);
  }

  public record ResolveRequest(String resolutionRule, String outcome) {}
  @PostMapping("/conflicts/{id}/resolve")
  Map<String,Object> resolve(@PathVariable UUID id, @RequestBody ResolveRequest r) {
    var p = principal();
    var cc = conflicts.findById(id).orElseThrow(() -> BizException.notFound("conflict_not_found","حالة التعارض غير موجودة"));
    if (!"open".equals(cc.getStatus())) throw BizException.conflict("already_resolved","التعارض محسوم");
    if (r.resolutionRule()==null||r.outcome()==null)
      throw BizException.badRequest("resolution_detail","الحل يتطلب قاعدة مجال ومراجعًا ونتيجة موثقة");
    cc.resolve(r.resolutionRule(), p.email(), r.outcome()); conflicts.save(cc);
    var tx = transactions.findById(cc.getTransactionId()).orElse(null);
    if (tx != null) { tx.setStatus("synced"); transactions.save(tx); }
    audit.record("offline.conflict.resolved","conflict_case",id.toString(),"{}");
    return Map.of("ok",true,"conflict",cc);
  }
  @GetMapping("/conflicts/open")
  Map<String,Object> openConflicts() {
    var rows = conflicts.findByStatusOrderByCreatedAtAsc("open");
    return Map.of("ok",true,"conflicts",rows,"count",rows.size());
  }
  @GetMapping("/packages/{id}/transactions")
  Map<String,Object> txList(@PathVariable UUID id) {
    pkg(id);
    var rows = transactions.findByPackageIdOrderByCreatedAtAsc(id);
    return Map.of("ok",true,"transactions",rows,"count",rows.size());
  }
}
