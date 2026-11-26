package eg.gov.health.platform.digitalquality;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.math.*; import java.util.*;
/** Digital experience quality (module 175). Task completion,
 *  errors and abandonment are measured; load time and stability
 *  are monitored; interface-to-API connectivity is verified
 *  against real data; interfaces are tested with actual users;
 *  and the cardinal rule is enforced — demonstration data must
 *  never appear as operational data, and a production surface
 *  carrying an unresolved demo-data incident is non-compliant
 *  until it is resolved. */
@RestController @RequestMapping("/api/digital-quality")
public class DigitalQualityController {
  private final UxQualityCheckRepository checks; private final DemoDataIncidentRepository incidents;
  private final InterfaceUserTestRepository userTests; private final AuditService audit;
  public DigitalQualityController(UxQualityCheckRepository checks, DemoDataIncidentRepository incidents,
                                  InterfaceUserTestRepository userTests, AuditService audit) {
    this.checks=checks; this.incidents=incidents; this.userTests=userTests; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }

  public record CheckRequest(String surface, String environment, BigDecimal taskCompletionPct, Integer errorCount,
                             Integer p95LoadMs, Boolean apiConnectivityVerified) {}
  @PostMapping("/checks")
  Map<String,Object> check(@RequestBody CheckRequest r) {
    var p = principal();
    boolean passed = DigitalQualityRules.checkPasses(r.taskCompletionPct() == null ? null : r.taskCompletionPct().doubleValue(),
      r.p95LoadMs(), Boolean.TRUE.equals(r.apiConnectivityVerified()));
    var c = checks.save(new UxQualityCheck(UUID.fromString(p.facilityId()), p.governorate(), r.surface(), r.environment(),
      r.taskCompletionPct(), r.errorCount(), r.p95LoadMs(), Boolean.TRUE.equals(r.apiConnectivityVerified()), passed));
    audit.record("dq.check.recorded","ux_quality_check",c.getId().toString(),"{}");
    return Map.of("ok",true,"check",c,"passed",passed,
      "gate","task completion >= 90%, p95 load <= 3000ms, API connectivity verified against real data");
  }
  public record IncidentRequest(String surface, String description) {}
  @PostMapping("/demo-incidents")
  Map<String,Object> incident(@RequestBody IncidentRequest r) {
    var p = principal();
    var i = incidents.save(new DemoDataIncident(UUID.fromString(p.facilityId()), p.governorate(), r.surface(), r.description()));
    audit.record("dq.demo_incident.detected","demo_data_incident",i.getId().toString(),"{}");
    return Map.of("ok",true,"incident",i,"effect","the production surface is NON-COMPLIANT until this incident is resolved — demo data is not operational data");
  }
  @PostMapping("/demo-incidents/{iid}/resolve")
  Map<String,Object> resolve(@PathVariable UUID iid) {
    principal();
    var i = incidents.findById(iid).orElseThrow(() -> BizException.notFound("incident_not_found","الحادث غير موجود"));
    i.resolve(); incidents.save(i);
    return Map.of("ok",true,"incident",i);
  }
  @GetMapping("/surfaces/{surface}/compliance")
  Map<String,Object> compliance(@PathVariable String surface, @RequestParam(defaultValue="production") String environment) {
    principal();
    long open = incidents.countBySurfaceAndResolvedFalse(surface);
    var list = checks.findBySurfaceOrderByCreatedAtDesc(surface);
    boolean latestPassed = !list.isEmpty() && list.get(0).isPassed();
    boolean compliant = DigitalQualityRules.surfaceCompliant(environment, open, latestPassed);
    return Map.of("ok",true,"surface",surface,"compliant",compliant,
      "unresolved_demo_incidents",open,"latest_check_passed",latestPassed);
  }
  public record UserTestRequest(String surface, Integer participants, BigDecimal taskSuccessPct, String findings) {}
  @PostMapping("/user-tests")
  Map<String,Object> userTest(@RequestBody UserTestRequest r) {
    var p = principal();
    var t = userTests.save(new InterfaceUserTest(UUID.fromString(p.facilityId()), p.governorate(), r.surface(), r.participants(), r.taskSuccessPct(), r.findings()));
    audit.record("dq.user_test.recorded","interface_user_test",t.getId().toString(),"{}");
    return Map.of("ok",true,"user_test",t);
  }
}
