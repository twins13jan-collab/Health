package eg.gov.health.platform.facilityenvironment;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.*; import java.math.*; import java.util.*;
/** Hygiene, environment & medical waste (module 106). Cleaning
 *  plans are risk-based per area and audited against a pass mark.
 *  Waste is segregated, weighed and manifested: regulated streams
 *  move only via a contractor licensed on the collection date, the
 *  chain of custody (generator → collector → receiver) completes
 *  before disposal is recorded, and infectious waste collected after
 *  48 hours of storage is flagged as a breach. Air, water and pest
 *  checks record measured values; failures carry actions. Hazardous
 *  spills open a timed response that closes only when contained AND
 *  decontaminated. */
@RestController @RequestMapping("/api/medical-waste")
public class MedicalWasteController {
  private final CleaningPlanRepository plans; private final CleaningAuditRepository audits;
  private final WasteManifestRepository manifests; private final EnvironmentalCheckRepository checks;
  private final SpillIncidentRepository spills; private final AuditService audit;
  public MedicalWasteController(CleaningPlanRepository plans, CleaningAuditRepository audits, WasteManifestRepository manifests,
                                EnvironmentalCheckRepository checks, SpillIncidentRepository spills, AuditService audit) {
    this.plans=plans; this.audits=audits; this.manifests=manifests; this.checks=checks; this.spills=spills; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private WasteManifest manifest(UUID id) {
    return manifests.findById(id).orElseThrow(() -> BizException.notFound("manifest_not_found","بيان النفايات غير موجود"));
  }

  public record PlanRequest(String area, String riskLevel, String frequency, String responsible) {}
  @PostMapping("/cleaning-plans")
  Map<String,Object> plan(@RequestBody PlanRequest r) {
    var p = principal();
    var pl = plans.save(new CleaningPlan(UUID.fromString(p.facilityId()), p.governorate(), r.area(), r.riskLevel(), r.frequency(), r.responsible()));
    audit.record("environment.cleaning.plan","cleaning_plan",pl.getId().toString(),"{}");
    return Map.of("ok",true,"plan",pl,"note","cleaning plan is risk-based: frequency follows the area risk level");
  }
  public record AuditRequest(UUID planId, String area, BigDecimal scorePct, String findings) {}
  @PostMapping("/cleaning-audits")
  Map<String,Object> auditCleaning(@RequestBody AuditRequest r) {
    var p = principal();
    var a = audits.save(new CleaningAudit(UUID.fromString(p.facilityId()), p.governorate(), r.planId(), r.area(), r.scorePct(), p.email(), r.findings()));
    audit.record("environment.cleaning.audited","cleaning_audit",a.getId().toString(),"{}");
    return Map.of("ok",true,"audit",a,"passed",Boolean.TRUE.equals(a.getPassed()),"pass_mark_pct",85);
  }
  public record ManifestRequest(String wasteType, String sourceUnit, BigDecimal weightKg, Integer containers) {}
  @PostMapping("/manifests")
  Map<String,Object> manifest(@RequestBody ManifestRequest r) {
    var p = principal();
    if (r.weightKg()==null||r.weightKg().signum()<=0) throw BizException.badRequest("weight_required","النفايات توزن — بيان بلا وزن لا يتحرك");
    var m = manifests.save(new WasteManifest(UUID.fromString(p.facilityId()), p.governorate(), r.wasteType(), r.sourceUnit(), r.weightKg(), r.containers(), p.email()));
    audit.record("environment.waste.generated","waste_manifest",m.getId().toString(),"{}");
    return Map.of("ok",true,"manifest",m);
  }
  public record CollectRequest(String contractor, String contractorLicense, LocalDate licenseValidUntil, OffsetDateTime collectedAt) {}
  @PostMapping("/manifests/{id}/collect")
  Map<String,Object> collect(@PathVariable UUID id, @RequestBody CollectRequest r) {
    var p = principal(); var m = manifest(id);
    OffsetDateTime at = r.collectedAt()==null ? OffsetDateTime.now() : r.collectedAt();
    // Regulated waste moves only with a contractor licensed on the collection date.
    if (!WasteRules.contractorLicensed(m.getWasteType(), r.contractorLicense(), r.licenseValidUntil(), at.toLocalDate()))
      throw BizException.conflict("unlicensed_contractor","النفايات المنظمة لا تتحرك إلا بمتعهد مرخص ساري الترخيص يوم الجمع");
    boolean breached = WasteRules.storageBreached(m.getWasteType(), m.getGeneratedAt(), at);
    m.collect(r.contractor(), r.contractorLicense(), r.licenseValidUntil(), p.email(), at); manifests.save(m);
    audit.record("environment.waste.collected","waste_manifest",id.toString(),"{}");
    return Map.of("ok",true,"manifest",m,"storage_breach_over_48h",breached);
  }
  public record DisposeRequest(String disposalMethod, OffsetDateTime disposedAt) {}
  @PostMapping("/manifests/{id}/dispose")
  Map<String,Object> dispose(@PathVariable UUID id, @RequestBody DisposeRequest r) {
    var p = principal(); var m = manifest(id);
    if (!"collected".equals(m.getStatus()))
      throw BizException.conflict("not_collected","التخلص لا يسبق الجمع أبدًا");
    // The receiver's signature completes the chain of custody.
    m.dispose(p.email(), r.disposalMethod(), r.disposedAt()==null?OffsetDateTime.now():r.disposedAt()); manifests.save(m);
    audit.record("environment.waste.disposed","waste_manifest",id.toString(),"{}");
    return Map.of("ok",true,"manifest",m,"chain_complete",WasteRules.chainComplete(m.getGeneratedBy(), m.getCollectedBy(), m.getReceivedBy()));
  }
  public record CheckRequest(String checkType, String location, String result, String measuredValue, String standardRef, String actionTaken) {}
  @PostMapping("/environmental-checks")
  Map<String,Object> check(@RequestBody CheckRequest r) {
    var p = principal();
    if ("fail".equals(r.result()) && (r.actionTaken()==null||r.actionTaken().isBlank()))
      throw BizException.badRequest("action_required","فحص بيئي فاشل بلا إجراء موثق لا يغلق");
    var c = checks.save(new EnvironmentalCheck(UUID.fromString(p.facilityId()), p.governorate(), r.checkType(), r.location(), r.result(), r.measuredValue(), r.standardRef(), p.email(), r.actionTaken()));
    audit.record("environment.check.recorded","environmental_check",c.getId().toString(),"{}");
    return Map.of("ok",true,"check",c);
  }
  public record SpillRequest(String substance, String location, String severity) {}
  @PostMapping("/spills")
  Map<String,Object> spill(@RequestBody SpillRequest r) {
    var p = principal();
    var s = spills.save(new SpillIncident(UUID.fromString(p.facilityId()), p.governorate(), r.substance(), r.location(), r.severity(), p.email()));
    audit.record("environment.spill.reported","spill_incident",s.getId().toString(),"{}");
    return Map.of("ok",true,"spill",s,"note","hazardous spill response started and timed");
  }
  @PostMapping("/spills/{id}/contain")
  Map<String,Object> contain(@PathVariable UUID id, @RequestBody Map<String,Object> body) {
    var s = spills.findById(id).orElseThrow(() -> BizException.notFound("spill_not_found","الانسكاب غير موجود"));
    s.contain(OffsetDateTime.now(), Boolean.TRUE.equals(body.get("decontaminated"))); spills.save(s);
    audit.record("environment.spill.contained","spill_incident",id.toString(),"{}");
    return Map.of("ok",true,"spill",s);
  }
  @PostMapping("/spills/{id}/close")
  Map<String,Object> closeSpill(@PathVariable UUID id) {
    var s = spills.findById(id).orElseThrow(() -> BizException.notFound("spill_not_found","الانسكاب غير موجود"));
    if (!WasteRules.spillClosable(s.getContainedAt(), s.isDecontaminated()))
      throw BizException.conflict("not_closable","الانسكاب يغلق بعد الاحتواء والتطهير معًا");
    s.close(); spills.save(s);
    audit.record("environment.spill.closed","spill_incident",id.toString(),"{}");
    return Map.of("ok",true,"spill",s);
  }
}
