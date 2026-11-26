package eg.gov.health.platform.labsafety;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.math.*; import java.time.*; import java.util.*;
/** Laboratory & diagnostic-device safety (module 158). Risk is
 *  assessed before a test is introduced; accuracy, sensitivity
 *  and interferences are verified; results are compared across
 *  analyzers; reagent lots and expiry are managed; and the impact
 *  of analyzer failures on previously reported results is
 *  assessed before the device returns to service. */
@RestController @RequestMapping("/api/lab-safety")
public class LabSafetyController {
  private final TestIntroductionRepository introductions; private final AnalyzerComparisonRepository comparisons;
  private final ReagentLotRepository lots; private final AnalyzerFailureRepository failures; private final AuditService audit;
  public LabSafetyController(TestIntroductionRepository introductions, AnalyzerComparisonRepository comparisons,
                             ReagentLotRepository lots, AnalyzerFailureRepository failures, AuditService audit) {
    this.introductions=introductions; this.comparisons=comparisons; this.lots=lots; this.failures=failures; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private TestIntroduction intro(UUID id) {
    return introductions.findById(id).orElseThrow(() -> BizException.notFound("introduction_not_found","طلب الإدخال غير موجود"));
  }

  public record IntroRequest(String testName, String analyzer, Boolean riskAssessmentDone, Boolean accuracyVerified,
                             Boolean sensitivityVerified, Boolean interferencesChecked) {}
  @PostMapping("/introductions")
  Map<String,Object> introduce(@RequestBody IntroRequest r) {
    var p = principal();
    var t = introductions.save(new TestIntroduction(UUID.fromString(p.facilityId()), p.governorate(), r.testName(), r.analyzer(),
      Boolean.TRUE.equals(r.riskAssessmentDone()), Boolean.TRUE.equals(r.accuracyVerified()),
      Boolean.TRUE.equals(r.sensitivityVerified()), Boolean.TRUE.equals(r.interferencesChecked())));
    audit.record("labsafety.introduction.opened","test_introduction",t.getId().toString(),"{}");
    return Map.of("ok",true,"introduction",t);
  }
  @PostMapping("/introductions/{id}/approve")
  Map<String,Object> approve(@PathVariable UUID id) {
    principal(); var t = intro(id);
    if (!LabSafetyRules.introductionApprovable(t.isRiskAssessmentDone(), t.isAccuracyVerified(), t.isSensitivityVerified(), t.isInterferencesChecked()))
      throw BizException.conflict("introduction_incomplete","إدخال اختبار جديد يتطلب تقييم مخاطر مسبقًا وتحققًا من الدقة والحساسية والتداخلات — اختبار لم يُتحقق منه ينتج أرقامًا لا يجوز التصرف عليها");
    t.approve(); introductions.save(t);
    audit.record("labsafety.introduction.approved","test_introduction",id.toString(),"{}");
    return Map.of("ok",true,"introduction",t,"live",true);
  }
  public record ComparisonRequest(String analyzerA, String analyzerB, BigDecimal biasPct) {}
  @PostMapping("/introductions/{id}/comparisons")
  Map<String,Object> compare(@PathVariable UUID id, @RequestBody ComparisonRequest r) {
    var p = principal(); intro(id);
    boolean ok = LabSafetyRules.comparisonAcceptable(r.biasPct() == null ? null : r.biasPct().doubleValue());
    var c = comparisons.save(new AnalyzerComparison(UUID.fromString(p.facilityId()), p.governorate(), id, r.analyzerA(), r.analyzerB(), r.biasPct(), ok));
    return Map.of("ok",true,"comparison",c,"acceptable",ok,"limit","inter-analyzer bias within ±10%");
  }
  public record LotRequest(String lotNo, LocalDate expiryDate) {}
  @PostMapping("/introductions/{id}/reagent-lots")
  Map<String,Object> lot(@PathVariable UUID id, @RequestBody LotRequest r) {
    var p = principal(); intro(id);
    var l = lots.save(new ReagentLot(UUID.fromString(p.facilityId()), p.governorate(), id, r.lotNo(), r.expiryDate()));
    boolean usable = LabSafetyRules.lotUsable(r.expiryDate(), LocalDate.now());
    return Map.of("ok",true,"lot",l,"usable_today",usable,"note",usable ? "within validity" : "EXPIRED — an expired lot is not a reagent, it is a hazard");
  }
  public record FailureRequest(String analyzer, String failureDescription) {}
  @PostMapping("/failures")
  Map<String,Object> fail(@RequestBody FailureRequest r) {
    var p = principal();
    var f = failures.save(new AnalyzerFailure(UUID.fromString(p.facilityId()), p.governorate(), r.analyzer(), r.failureDescription()));
    audit.record("labsafety.analyzer.failed","analyzer_failure",f.getId().toString(),"{}");
    return Map.of("ok",true,"failure",f,"effect","analyzer out of service until the impact on previous results is assessed");
  }
  public record ImpactRequest(Integer affectedResultsCount) {}
  @PostMapping("/failures/{fid}/assess-impact")
  Map<String,Object> impact(@PathVariable UUID fid, @RequestBody ImpactRequest r) {
    principal();
    var f = failures.findById(fid).orElseThrow(() -> BizException.notFound("failure_not_found","العطل غير موجود"));
    f.assessImpact(r.affectedResultsCount() == null ? 0 : r.affectedResultsCount()); failures.save(f);
    audit.record("labsafety.impact.assessed","analyzer_failure",fid.toString(),"{}");
    return Map.of("ok",true,"failure",f);
  }
  @PostMapping("/failures/{fid}/return-to-service")
  Map<String,Object> returnToService(@PathVariable UUID fid) {
    principal();
    var f = failures.findById(fid).orElseThrow(() -> BizException.notFound("failure_not_found","العطل غير موجود"));
    if (!LabSafetyRules.returnToServicePermitted(f.isImpactAssessed()))
      throw BizException.conflict("impact_unassessed","عودة الجهاز للخدمة بعد تقييم أثر العطل على النتائج السابقة — السؤال ليس «هل أُصلح» فقط");
    f.returnToService(); failures.save(f);
    return Map.of("ok",true,"failure",f,"returned",true);
  }
}
