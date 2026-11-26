package eg.gov.health.platform.uxgov;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.math.*; import java.util.*;
/** User requirements & UX governance (module 136). User roles and
 *  their work scenarios are managed explicitly against a unified
 *  design library. Critical tasks carry time targets: a critical
 *  scenario slower than its target is flagged, because speed of
 *  critical work is a safety property. Usability is tested and
 *  documented — a scenario validates only on a completed test
 *  session with no open blocker findings. */
@RestController @RequestMapping("/api/ux-governance")
public class UxGovernanceController {
  private final UxScenarioRepository scenarios; private final UsabilityTestSessionRepository sessions;
  private final UxFindingRepository findings; private final AuditService audit;
  public UxGovernanceController(UxScenarioRepository scenarios, UsabilityTestSessionRepository sessions,
                                UxFindingRepository findings, AuditService audit) {
    this.scenarios=scenarios; this.sessions=sessions; this.findings=findings; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private UxScenario scenario(UUID id) {
    return scenarios.findById(id).orElseThrow(() -> BizException.notFound("scenario_not_found","السيناريو غير موجود"));
  }

  public record ScenarioRequest(String userRole, String scenarioName, Boolean criticalTask, Integer targetSeconds, Integer measuredSeconds) {}
  @PostMapping("/scenarios")
  Map<String,Object> create(@RequestBody ScenarioRequest r) {
    var p = principal();
    var s = scenarios.save(new UxScenario(UUID.fromString(p.facilityId()), p.governorate(), r.userRole(), r.scenarioName(),
      Boolean.TRUE.equals(r.criticalTask()), r.targetSeconds(), r.measuredSeconds()));
    audit.record("ux.scenario.defined","ux_scenario",s.getId().toString(),"{}");
    return Map.of("ok",true,"scenario",s,
      "speed_flagged",!UxRules.criticalTaskFastEnough(s.isCriticalTask(), s.getTargetSeconds(), s.getMeasuredSeconds()),
      "speed_note","a critical task slower than its target is an optimization — and safety — finding");
  }
  public record SessionRequest(String participantRole, Boolean taskCompleted, Integer completionSeconds, Integer errorsCount, String notes, BigDecimal satisfactionScore) {}
  @PostMapping("/scenarios/{id}/sessions")
  Map<String,Object> session(@PathVariable UUID id, @RequestBody SessionRequest r) {
    var p = principal(); scenario(id);
    var ss = sessions.save(new UsabilityTestSession(UUID.fromString(p.facilityId()), p.governorate(), id, p.email(), r.participantRole(),
      Boolean.TRUE.equals(r.taskCompleted()), r.completionSeconds(), r.errorsCount(), r.notes(), r.satisfactionScore()));
    audit.record("ux.session.recorded","usability_test_session",ss.getId().toString(),"{}");
    return Map.of("ok",true,"session",ss);
  }
  public record FindingRequest(String severity, String description) {}
  @PostMapping("/scenarios/{id}/findings")
  Map<String,Object> finding(@PathVariable UUID id, @RequestBody FindingRequest r) {
    var p = principal(); scenario(id);
    var f = findings.save(new UxFinding(UUID.fromString(p.facilityId()), p.governorate(), id, r.severity(), r.description()));
    audit.record("ux.finding.raised","ux_finding",f.getId().toString(),"{}");
    return Map.of("ok",true,"finding",f);
  }
  public record AddressRequest(String resolution) {}
  @PostMapping("/findings/{fid}/address")
  Map<String,Object> address(@PathVariable UUID fid, @RequestBody AddressRequest r) {
    principal();
    var f = findings.findById(fid).orElseThrow(() -> BizException.notFound("finding_not_found","الملاحظة غير موجودة"));
    f.address(r.resolution()); findings.save(f);
    return Map.of("ok",true,"finding",f);
  }
  @PostMapping("/scenarios/{id}/validate")
  Map<String,Object> validate(@PathVariable UUID id) {
    principal(); var s = scenario(id);
    boolean completed = sessions.existsByScenarioIdAndTaskCompletedTrue(id);
    long blockers = findings.countByScenarioIdAndSeverityAndStatus(id, "blocker", "open");
    if (!UxRules.scenarioValidatable(completed, blockers))
      throw BizException.conflict("not_validatable","الاعتماد يتطلب جلسة اختبار مكتملة وعدم وجود ملاحظات حاجبة مفتوحة");
    s.setStatus("validated"); scenarios.save(s);
    audit.record("ux.scenario.validated","ux_scenario",id.toString(),"{}");
    return Map.of("ok",true,"scenario",s);
  }
}
