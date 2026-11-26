package eg.gov.health.platform.nfr;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.*; import java.util.*;
/** Non-functional requirements (module 176). Performance,
 *  scalability, availability and resilience defined per service;
 *  privacy, security, audit and retention defined; compatibility
 *  and portability defined; usability and offline operation
 *  defined; and load, file and message limits defined — each
 *  proven by fresh measurement, not by assertion. */
@RestController @RequestMapping("/api/nfr")
public class NfrController {
  private final NfrRequirementRepository requirements; private final NfrMeasurementRepository measurements; private final AuditService audit;
  public NfrController(NfrRequirementRepository requirements, NfrMeasurementRepository measurements, AuditService audit) {
    this.requirements=requirements; this.measurements=measurements; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private String statusOf(NfrRequirement r) {
    var ms = measurements.findByRequirementIdOrderByMeasuredOnDesc(r.getId());
    if (ms.isEmpty()) return "unverified";
    var latest = ms.get(0);
    return NfrRules.status(latest.getMeasuredValue(), r.getTargetValue(), r.getComparison(), latest.getMeasuredOn(), LocalDate.now());
  }

  public record ReqBody(String serviceRef, String category, String metric, Double targetValue, String comparison, Boolean critical) {}
  @PostMapping("/requirements")
  Map<String,Object> create(@RequestBody ReqBody r) {
    var p = principal();
    var req = requirements.save(new NfrRequirement(UUID.fromString(p.facilityId()), p.governorate(), r.serviceRef(), r.category(), r.metric(), r.targetValue(), r.comparison(), Boolean.TRUE.equals(r.critical())));
    return Map.of("ok",true,"requirement",req,"status","unverified","note","defined is not met — only a fresh measurement proves an NFR");
  }
  public record MeasureBody(Double measuredValue, LocalDate measuredOn) {}
  @PostMapping("/requirements/{id}/measurements")
  Map<String,Object> measure(@PathVariable UUID id, @RequestBody MeasureBody r) {
    var p = principal();
    var req = requirements.findById(id).orElseThrow(() -> BizException.notFound("requirement_not_found","المتطلب غير موجود"));
    var m = measurements.save(new NfrMeasurement(UUID.fromString(p.facilityId()), p.governorate(), id, r.measuredValue(), r.measuredOn()));
    String st = NfrRules.status(r.measuredValue(), req.getTargetValue(), req.getComparison(), r.measuredOn(), LocalDate.now());
    return Map.of("ok",true,"measurement",m,"status",st);
  }
  @GetMapping("/services/{serviceRef}/readiness")
  Map<String,Object> readiness(@PathVariable String serviceRef) {
    principal();
    var reqs = requirements.findByServiceRef(serviceRef);
    List<Map<String,Object>> detail = new ArrayList<>();
    boolean ready = true;
    for (var r : reqs) {
      String st = statusOf(r);
      if (r.isCritical() && !"met".equals(st)) ready = false;
      detail.add(Map.of("requirement_id", r.getId().toString(), "status", st, "critical", r.isCritical()));
    }
    return Map.of("ok",true,"service_ref",serviceRef,"ready",ready,"requirements",detail,
      "rule","a service with an unmet or unverified critical NFR is not ready — features do not compensate for availability or security");
  }
}
