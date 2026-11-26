package eg.gov.health.platform.institutionalrisk;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.*; import java.util.*;
/** Institutional risk & decisions (module 134). A register of
 *  clinical, operational, financial, legal and technical risks;
 *  likelihood, impact, controls and residual risk assessed; a
 *  named risk owner and a treatment plan for each; committee
 *  decisions and minutes managed; and implementation tracked
 *  with its impact measured. */
@RestController @RequestMapping("/api/institutional-risk")
public class InstitutionalRiskController {
  private final InstitutionalRiskRepository risks; private final RiskTreatmentRepository treatments;
  private final RiskCommitteeDecisionRepository decisions; private final AuditService audit;
  public InstitutionalRiskController(InstitutionalRiskRepository risks, RiskTreatmentRepository treatments,
                                     RiskCommitteeDecisionRepository decisions, AuditService audit) {
    this.risks=risks; this.treatments=treatments; this.decisions=decisions; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private InstitutionalRisk risk(UUID id) {
    return risks.findById(id).orElseThrow(() -> BizException.notFound("risk_not_found","الخطر غير موجود"));
  }

  public record RiskBody(String category, String title, Integer likelihood, Integer impact, String controls,
                         String ownerRef, String treatmentPlan, Integer residualLikelihood, Integer residualImpact, String acceptedBy) {}
  @PostMapping("/risks")
  Map<String,Object> create(@RequestBody RiskBody r) {
    var p = principal();
    var risk = risks.save(new InstitutionalRisk(UUID.fromString(p.facilityId()), p.governorate(), r.category(), r.title(),
      r.likelihood(), r.impact(), r.controls(), r.ownerRef(), r.treatmentPlan(), r.residualLikelihood(), r.residualImpact(), r.acceptedBy()));
    return Map.of("ok",true,"risk",risk,"inherent_score",InstitutionalRiskRules.score(r.likelihood(), r.impact()),
      "residual_score",InstitutionalRiskRules.score(r.residualLikelihood(), r.residualImpact()));
  }
  public record TreatmentBody(String action, LocalDate dueDate) {}
  @PostMapping("/risks/{id}/treatments")
  Map<String,Object> treatment(@PathVariable UUID id, @RequestBody TreatmentBody r) {
    var p = principal(); risk(id);
    var t = treatments.save(new RiskTreatment(UUID.fromString(p.facilityId()), p.governorate(), id, r.action(), r.dueDate()));
    return Map.of("ok",true,"treatment",t);
  }
  @PostMapping("/treatments/{tid}/done")
  Map<String,Object> done(@PathVariable UUID tid) {
    principal();
    var t = treatments.findById(tid).orElseThrow(() -> BizException.notFound("treatment_not_found","المعالجة غير موجودة"));
    t.complete(); treatments.save(t);
    return Map.of("ok",true,"treatment",t);
  }
  @PostMapping("/risks/{id}/close")
  Map<String,Object> close(@PathVariable UUID id) {
    principal(); var risk = risk(id);
    boolean allDone = treatments.findByRiskId(id).stream().allMatch(RiskTreatment::isDone);
    if (!InstitutionalRiskRules.closable(risk.getOwnerRef(), risk.getTreatmentPlan(), allDone,
        risk.getResidualLikelihood(), risk.getResidualImpact(), risk.getAcceptedBy()))
      throw BizException.conflict("risk_not_closable","خطر لا يُغلق إلا بمالك مسمى وخطة معالجة منفذة كاملة ومخاطرة متبقية دون القصوى — أو قبول رسمي موثق للقصوى من سلطة مسماة");
    risk.close(); risks.save(risk);
    audit.record("irisk.closed","institutional_risk",id.toString(),"{}");
    return Map.of("ok",true,"risk",risk);
  }
  public record DecisionBody(String decisionRef, String summary, String ownerRef, LocalDate dueDate) {}
  @PostMapping("/decisions")
  Map<String,Object> decision(@RequestBody DecisionBody r) {
    var p = principal();
    var d = decisions.save(new RiskCommitteeDecision(UUID.fromString(p.facilityId()), p.governorate(), r.decisionRef(), r.summary(), r.ownerRef(), r.dueDate()));
    return Map.of("ok",true,"decision_entry",d,"tracking","a committee decision is tracked until implemented AND its impact measured");
  }
  @PostMapping("/decisions/{did}/implement")
  Map<String,Object> implement(@PathVariable UUID did) {
    principal();
    var d = decisions.findById(did).orElseThrow(() -> BizException.notFound("decision_not_found","القرار غير موجود"));
    d.implement(); decisions.save(d);
    return Map.of("ok",true,"decision_entry",d);
  }
  @PostMapping("/decisions/{did}/measure-impact")
  Map<String,Object> measureImpact(@PathVariable UUID did) {
    principal();
    var d = decisions.findById(did).orElseThrow(() -> BizException.notFound("decision_not_found","القرار غير موجود"));
    if (!d.isImplemented())
      throw BizException.conflict("not_implemented","أثر قرار لم يُنفذ بعد لا يُقاس");
    d.measureImpact(); decisions.save(d);
    return Map.of("ok",true,"decision_entry",d);
  }
}
