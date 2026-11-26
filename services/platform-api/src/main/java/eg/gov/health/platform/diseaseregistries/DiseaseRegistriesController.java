package eg.gov.health.platform.diseaseregistries;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Disease registries & real-world research (module 174).
 *  Registries run on unified case definitions; data is linked
 *  to outcomes and follow-up; research protocols and approvals
 *  are managed; data quality, loss to follow-up and bias are
 *  managed openly; and aggregate results are published. */
@RestController @RequestMapping("/api/disease-registries")
public class DiseaseRegistriesController {
  private final DiseaseRegistryRepository registries; private final RegistryCaseRepository cases;
  private final RegistryPublicationRepository publications; private final AuditService audit;
  public DiseaseRegistriesController(DiseaseRegistryRepository registries, RegistryCaseRepository cases,
                                     RegistryPublicationRepository publications, AuditService audit) {
    this.registries=registries; this.cases=cases; this.publications=publications; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private DiseaseRegistry registry(UUID id) {
    return registries.findById(id).orElseThrow(() -> BizException.notFound("registry_not_found","السجل غير موجود"));
  }

  public record RegistryBody(String name, String caseDefinitionRef) {}
  @PostMapping("/registries")
  Map<String,Object> create(@RequestBody RegistryBody r) {
    var p = principal();
    if (r.caseDefinitionRef() == null || r.caseDefinitionRef().isBlank())
      throw BizException.badRequest("definition_required","سجل أمراض بلا تعريف حالة موحد ليس سجلًا — إنه كومة أسماء");
    var reg = registries.save(new DiseaseRegistry(UUID.fromString(p.facilityId()), p.governorate(), r.name(), r.caseDefinitionRef()));
    return Map.of("ok",true,"registry",reg);
  }
  public record CaseBody(String patientRef, Boolean meetsDefinition, Boolean outcomeRecorded, String followupStatus) {}
  @PostMapping("/registries/{id}/cases")
  Map<String,Object> enroll(@PathVariable UUID id, @RequestBody CaseBody r) {
    var p = principal(); registry(id);
    if (!Boolean.TRUE.equals(r.meetsDefinition()))
      throw BizException.conflict("definition_not_met","حالة لا تستوفي تعريف الحالة الموحد لا تدخل السجل");
    var c = cases.save(new RegistryCase(UUID.fromString(p.facilityId()), p.governorate(), id, r.patientRef(), true,
      Boolean.TRUE.equals(r.outcomeRecorded()), r.followupStatus() == null ? "active" : r.followupStatus()));
    return Map.of("ok",true,"registry_case",c);
  }
  public record PublishBody(String title, String biasNote) {}
  @PostMapping("/registries/{id}/publish")
  Map<String,Object> publish(@PathVariable UUID id, @RequestBody PublishBody r) {
    var p = principal(); registry(id);
    var all = cases.findByRegistryId(id);
    long total = all.size();
    long lost = all.stream().filter(c -> "lost".equals(c.getFollowupStatus())).count();
    double lostRate = RegistryRules.lostRatePct(total, lost);
    if (!RegistryRules.publishable(total, lostRate, r.biasNote()))
      throw BizException.conflict("not_publishable","نشر مجمع يتطلب مجموعة ≥30 وفقد متابعة معلنًا — فوق 30% فقدًا مذكرة تحيز إلزامية");
    var pub = publications.save(new RegistryPublication(UUID.fromString(p.facilityId()), p.governorate(), id, r.title(), r.biasNote(), lostRate));
    audit.record("registry.results.published","registry_publication",pub.getId().toString(),"{}");
    return Map.of("ok",true,"publication",pub,"total_cases",total,"lost_to_followup",lost,"lost_rate_pct",lostRate,"aggregate_only",true);
  }
}
