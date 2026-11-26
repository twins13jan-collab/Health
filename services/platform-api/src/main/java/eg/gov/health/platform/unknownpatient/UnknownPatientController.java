package eg.gov.health.platform.unknownpatient;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Unknown-identity patients & humanitarian cases (module 185).
 *  Care starts immediately under a safe temporary identity —
 *  treatment never waits for documents. The temporary identity
 *  supports the unconscious, found persons, separated children and
 *  the cognitively impaired. Merging into the verified record needs
 *  documented verification evidence and an independent verifier —
 *  never the temporary identity's creator — and a merged identity
 *  closes to further change. Social-protection data is restricted
 *  to social-work and administration roles. Every verification and
 *  modification step is preserved in a chain. Deaths without
 *  documents follow the legal procedure, authority notified. */
@RestController @RequestMapping("/api/unknown-patients")
public class UnknownPatientController {
  private final TemporaryIdentityRepository temps; private final IdentityMergeRecordRepository merges;
  private final IdentityVerificationChainRepository chain; private final UnidentifiedDeathRecordRepository deaths;
  private final AuditService audit;
  public UnknownPatientController(TemporaryIdentityRepository temps, IdentityMergeRecordRepository merges,
                                  IdentityVerificationChainRepository chain, UnidentifiedDeathRecordRepository deaths, AuditService audit) {
    this.temps=temps; this.merges=merges; this.chain=chain; this.deaths=deaths; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private TemporaryIdentity temp(UUID id) {
    return temps.findById(id).orElseThrow(() -> BizException.notFound("temp_not_found","الهوية المؤقتة غير موجودة"));
  }
  private void chain(UUID fac, String gov, UUID tempId, String step, String actor, String detail) {
    chain.save(new IdentityVerificationChain(fac, gov, tempId, step, actor, detail));
  }

  public record TempRequest(String tempCode, String description, String circumstance, Boolean protectionFlag) {}
  @PostMapping("/")
  Map<String,Object> create(@RequestBody TempRequest r) {
    var p = principal();
    boolean protection = Boolean.TRUE.equals(r.protectionFlag()) || "separated_child".equals(r.circumstance());
    var t = temps.save(new TemporaryIdentity(UUID.fromString(p.facilityId()), p.governorate(), r.tempCode(), r.description(), r.circumstance(), protection, p.email()));
    chain(t.getFacilityId(), p.governorate(), t.getId(), "created", p.email(), "temporary identity opened — care starts without documents");
    audit.record("unknown.identity.created","temporary_identity",t.getId().toString(),"{}");
    return Map.of("ok",true,"identity",t,"protection_restricted",protection);
  }
  @GetMapping("/{id}")
  Map<String,Object> get(@PathVariable UUID id) {
    var p = principal(); var t = temp(id);
    // Social-protection data is visible only to social-work and administration roles.
    if (!UnknownPatientRules.protectionVisible(t.isProtectionFlag(), p.role()))
      throw BizException.forbidden("بيانات الحماية الاجتماعية مقيدة لأدوار الخدمة الاجتماعية والإدارة");
    return Map.of("ok",true,"identity",t,"verification_chain",chain.findByTempIdOrderByCreatedAtAsc(id));
  }
  public record MergeRequest(String verifiedPatientRef, String verificationEvidence, String verifiedBy) {}
  @PostMapping("/{id}/merge")
  Map<String,Object> merge(@PathVariable UUID id, @RequestBody MergeRequest r) {
    var p = principal(); var t = temp(id);
    if (!UnknownPatientRules.mergeAllowed(t.getStatus(), r.verificationEvidence(), r.verifiedBy(), t.getCreatedBy()))
      throw BizException.conflict("merge_blocked","الدمج يتطلب هوية مؤقتة نشطة وأدلة تحقق موثقة ومتحققًا مستقلًا عن منشئ الهوية المؤقتة");
    var m = merges.save(new IdentityMergeRecord(t.getFacilityId(), p.governorate(), id, r.verifiedPatientRef(), r.verificationEvidence(), r.verifiedBy(), p.email()));
    t.setStatus("merged"); temps.save(t);
    chain(t.getFacilityId(), p.governorate(), id, "merged", p.email(), "merged into " + r.verifiedPatientRef() + " on evidence verified by " + r.verifiedBy());
    audit.record("unknown.identity.merged","identity_merge_record",m.getId().toString(),"{}");
    return Map.of("ok",true,"merge",m,"identity_status","merged");
  }
  public record DeathRequest(String legalProcedureRef, Boolean authorityNotified) {}
  @PostMapping("/{id}/unidentified-death")
  Map<String,Object> death(@PathVariable UUID id, @RequestBody DeathRequest r) {
    var p = principal(); var t = temp(id);
    if (r.legalProcedureRef()==null || !Boolean.TRUE.equals(r.authorityNotified()))
      throw BizException.badRequest("legal_procedure","وفاة دون وثائق تتبع الإجراء القانوني مع إخطار الجهة المختصة");
    var d = deaths.save(new UnidentifiedDeathRecord(t.getFacilityId(), p.governorate(), id, r.legalProcedureRef(), true));
    t.setStatus("deceased_unidentified"); temps.save(t);
    chain(t.getFacilityId(), p.governorate(), id, "death_recorded", p.email(), "legal procedure " + r.legalProcedureRef());
    audit.record("unknown.death.recorded","unidentified_death_record",d.getId().toString(),"{}");
    return Map.of("ok",true,"death_record",d);
  }
}
