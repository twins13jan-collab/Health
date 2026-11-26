package eg.gov.health.platform.clinicalriskmatrix;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Clinical & medication risk matrix (module 178). Hazards of
 *  identity, medication, dose and procedure; hazards of critical
 *  results and delay; hazards of transport, handover and device
 *  outage — each with prevention, detection and response
 *  controls; and false-alarm and alert-fatigue review. */
@RestController @RequestMapping("/api/clinical-risk-matrix")
public class ClinicalRiskMatrixController {
  private final MatrixRiskRepository risks; private final MatrixAlertRuleRepository alerts; private final AuditService audit;
  public ClinicalRiskMatrixController(MatrixRiskRepository risks, MatrixAlertRuleRepository alerts, AuditService audit) {
    this.risks=risks; this.alerts=alerts; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }

  public record RiskBody(String hazardDomain, String title, Integer likelihood, Integer severity,
                         String controlsPrevention, String controlsDetection, String controlsResponse) {}
  @PostMapping("/risks")
  Map<String,Object> create(@RequestBody RiskBody r) {
    var p = principal();
    var risk = risks.save(new MatrixRisk(UUID.fromString(p.facilityId()), p.governorate(), r.hazardDomain(), r.title(),
      r.likelihood(), r.severity(), r.controlsPrevention(), r.controlsDetection(), r.controlsResponse()));
    int pr = MatrixRules.priority(r.likelihood(), r.severity());
    return Map.of("ok",true,"matrix_risk",risk,"priority",pr,"band",MatrixRules.band(pr),
      "fully_controlled",MatrixRules.fullyControlled(r.controlsPrevention(), r.controlsDetection(), r.controlsResponse()));
  }
  @PostMapping("/risks/{id}/mark-controlled")
  Map<String,Object> markControlled(@PathVariable UUID id) {
    principal();
    var risk = risks.findById(id).orElseThrow(() -> BizException.notFound("risk_not_found","الخطر غير موجود"));
    if (!MatrixRules.fullyControlled(risk.getControlsPrevention(), risk.getControlsDetection(), risk.getControlsResponse()))
      throw BizException.conflict("control_layer_missing","خطر لا يُوسم مضبوطًا وطبقة ضوابط ناقصة — المنع والكشف والاستجابة ثلاثتها");
    risk.markControlled(); risks.save(risk);
    return Map.of("ok",true,"matrix_risk",risk);
  }
  public record AlertBody(String alertName, Integer totalFired, Integer falsePositiveCount) {}
  @PostMapping("/risks/{id}/alerts")
  Map<String,Object> alert(@PathVariable UUID id, @RequestBody AlertBody r) {
    var p = principal();
    risks.findById(id).orElseThrow(() -> BizException.notFound("risk_not_found","الخطر غير موجود"));
    var a = alerts.save(new MatrixAlertRule(UUID.fromString(p.facilityId()), p.governorate(), id, r.alertName(), r.totalFired(), r.falsePositiveCount()));
    boolean fatigued = MatrixRules.alertFatigued(r.totalFired(), r.falsePositiveCount());
    if (fatigued) { a.setStatus("under_review"); alerts.save(a); audit.record("matrix.alert.fatigue_review","matrix_alert_rule",a.getId().toString(),"{}"); }
    return Map.of("ok",true,"alert_rule",a,"fatigued",fatigued,
      "note",fatigued ? "false-positive rate above 50% on a real sample — the alert goes under review; an ignored alarm protects no one" : "within fatigue limits");
  }
}
