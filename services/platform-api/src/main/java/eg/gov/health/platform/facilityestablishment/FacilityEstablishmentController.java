package eg.gov.health.platform.facilityestablishment;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Facility establishment, equipping, licensing & opening
 *  (module 73). The population-need study and health map justify
 *  a new facility; site selection, architectural and engineering
 *  design, medical requirements and infection control govern it;
 *  contractors, contracts, extracts, changes, risks and execution
 *  quality are managed; equipment, furniture, infrastructure and
 *  IT systems are supplied, installed and tested; and handover,
 *  trial operation, licences, accreditations and staff training
 *  complete the opening. */
@RestController @RequestMapping("/api/facility-establishment")
public class FacilityEstablishmentController {
  private final EstablishmentProjectRepository projects; private final CommissioningCheckRepository checks;
  private final OpeningLicenceRepository licences; private final AuditService audit;
  public FacilityEstablishmentController(EstablishmentProjectRepository projects, CommissioningCheckRepository checks,
                                         OpeningLicenceRepository licences, AuditService audit) {
    this.projects=projects; this.checks=checks; this.licences=licences; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private EstablishmentProject project(UUID id) {
    return projects.findById(id).orElseThrow(() -> BizException.notFound("project_not_found","المشروع غير موجود"));
  }

  public record ProjectBody(String name, Boolean needsStudyDone, Boolean siteSelected, Boolean infectionControlDesignApproved) {}
  @PostMapping("/projects")
  Map<String,Object> create(@RequestBody ProjectBody r) {
    var p = principal();
    var pr = projects.save(new EstablishmentProject(UUID.fromString(p.facilityId()), p.governorate(), r.name(),
      Boolean.TRUE.equals(r.needsStudyDone()), Boolean.TRUE.equals(r.siteSelected()), Boolean.TRUE.equals(r.infectionControlDesignApproved())));
    return Map.of("ok",true,"project",pr);
  }
  public record CheckBody(String checkType, Boolean passed) {}
  @PostMapping("/projects/{id}/checks")
  Map<String,Object> check(@PathVariable UUID id, @RequestBody CheckBody r) {
    var p = principal(); project(id);
    var c = checks.save(new CommissioningCheck(UUID.fromString(p.facilityId()), p.governorate(), id, r.checkType(), Boolean.TRUE.equals(r.passed())));
    return Map.of("ok",true,"check",c);
  }
  public record LicenceBody(String licenceRef, Boolean granted) {}
  @PostMapping("/projects/{id}/licences")
  Map<String,Object> licence(@PathVariable UUID id, @RequestBody LicenceBody r) {
    var p = principal(); project(id);
    var l = licences.save(new OpeningLicence(UUID.fromString(p.facilityId()), p.governorate(), id, r.licenceRef(), Boolean.TRUE.equals(r.granted())));
    return Map.of("ok",true,"licence",l);
  }
  @PostMapping("/projects/{id}/open")
  Map<String,Object> open(@PathVariable UUID id) {
    principal(); var pr = project(id);
    Map<String,Boolean> passed = new HashMap<>();
    for (var c : checks.findByProjectId(id)) if (c.isPassed()) passed.put(c.getCheckType(), true);
    boolean licenceGranted = licences.findByProjectId(id).stream().anyMatch(OpeningLicence::isGranted);
    var missing = EstablishmentRules.missingGates(pr.isNeedsStudyDone(), pr.isSiteSelected(), pr.isInfectionControlDesignApproved(), passed, licenceGranted);
    if (!missing.isEmpty())
      throw BizException.conflict("opening_gates_missing","منشأة لا تُفتتح وبوابات ناقصة: " + String.join("، ", missing) + " — الشريط ليس بوابة");
    pr.open(); projects.save(pr);
    audit.record("establishment.opened","establishment_project",id.toString(),"{}");
    return Map.of("ok",true,"project",pr,"gates_all_passed",true);
  }
}
