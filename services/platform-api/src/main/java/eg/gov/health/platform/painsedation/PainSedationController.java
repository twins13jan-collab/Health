package eg.gov.health.platform.painsedation;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Pain, procedural sedation & analgesia (module 86). Pain is
 *  assessed with age- and condition-appropriate scales; analgesia
 *  plans cover acute, chronic, postoperative and palliative pain
 *  with controlled medications governed; procedural sedation
 *  outside the operating theatre documents the pre-procedure
 *  assessment, airway and risk evaluation and consent, and
 *  discharge follows monitored recovery criteria. */
@RestController @RequestMapping("/api/pain-sedation")
public class PainSedationController {
  private final PainAssessmentRepository pains; private final AnalgesiaPlanRepository plans;
  private final SedationSessionRepository sessions; private final AuditService audit;
  public PainSedationController(PainAssessmentRepository pains, AnalgesiaPlanRepository plans, SedationSessionRepository sessions, AuditService audit) {
    this.pains=pains; this.plans=plans; this.sessions=sessions; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private SedationSession session(UUID id) {
    return sessions.findById(id).orElseThrow(() -> BizException.notFound("session_not_found","جلسة التهدئة غير موجودة"));
  }

  public record PainRequest(String patientRef, String scale, Integer score, String ageGroup) {}
  @PostMapping("/pain-assessments")
  Map<String,Object> assessPain(@RequestBody PainRequest r) {
    var p = principal();
    if (!SedationRules.scaleAppropriate(r.scale(), r.ageGroup()))
      throw BizException.conflict("wrong_scale","مقياس غير مناسب للعمر والحالة: الرضيع غير الناطق يُقاس بـFLACC والأطفال بمقاييس الوجوه والبالغون رقميًا — المقياس الخاطئ قياس خاطئ");
    var a = pains.save(new PainAssessment(UUID.fromString(p.facilityId()), p.governorate(), r.patientRef(), r.scale(), r.score(), r.ageGroup()));
    return Map.of("ok",true,"assessment",a,"severe",r.score() != null && r.score() >= 7);
  }
  public record PlanRequest(String patientRef, String planType, Boolean controlledMedication) {}
  @PostMapping("/analgesia-plans")
  Map<String,Object> plan(@RequestBody PlanRequest r) {
    var p = principal();
    var pl = plans.save(new AnalgesiaPlan(UUID.fromString(p.facilityId()), p.governorate(), r.patientRef(), r.planType(),
      Boolean.TRUE.equals(r.controlledMedication()), p.email()));
    audit.record("sedation.plan.created","analgesia_plan",pl.getId().toString(),"{}");
    return Map.of("ok",true,"plan",pl,"controlled_register",Boolean.TRUE.equals(r.controlledMedication()) ? "controlled medication — entered in the controlled register" : "standard");
  }
  public record SessionRequest(String patientRef, String procedureName, Boolean preAssessmentDone, Boolean airwayAssessed, Boolean consentDocumented, String riskClass) {}
  @PostMapping("/sedation-sessions")
  Map<String,Object> createSession(@RequestBody SessionRequest r) {
    var p = principal();
    var s = sessions.save(new SedationSession(UUID.fromString(p.facilityId()), p.governorate(), r.patientRef(), r.procedureName(),
      Boolean.TRUE.equals(r.preAssessmentDone()), Boolean.TRUE.equals(r.airwayAssessed()), Boolean.TRUE.equals(r.consentDocumented()), r.riskClass()));
    return Map.of("ok",true,"session",s);
  }
  @PostMapping("/sedation-sessions/{id}/start")
  Map<String,Object> start(@PathVariable UUID id) {
    principal(); var s = session(id);
    if (!SedationRules.sedationStartPermitted(s.isPreAssessmentDone(), s.isAirwayAssessed(), s.isConsentDocumented()))
      throw BizException.conflict("sedation_blocked","التهدئة الإجرائية خارج غرف العمليات لا تبدأ قبل تقييم ما قبل الإجراء وتقييم مجرى الهواء والموافقة الموثقة");
    s.start(); sessions.save(s);
    audit.record("sedation.started","sedation_session",id.toString(),"{}");
    return Map.of("ok",true,"session",s,"monitoring","continuous SpO2 + capnography during and after sedation");
  }
  public record RecoveryRequest(Integer aldreteScore, String complications) {}
  @PostMapping("/sedation-sessions/{id}/recovery")
  Map<String,Object> recovery(@PathVariable UUID id, @RequestBody RecoveryRequest r) {
    principal(); var s = session(id);
    s.toRecovery(r.aldreteScore(), r.complications()); sessions.save(s);
    return Map.of("ok",true,"session",s);
  }
  @PostMapping("/sedation-sessions/{id}/discharge")
  Map<String,Object> discharge(@PathVariable UUID id) {
    principal(); var s = session(id);
    if (!SedationRules.dischargeSafe(s.getAldreteScore()))
      throw BizException.conflict("discharge_unsafe","الخروج الآمن بعد التهدئة يتطلب درجة إفاقة Aldrete لا تقل عن 9 — الأدنى يبقى تحت المراقبة");
    s.discharge(); sessions.save(s);
    audit.record("sedation.discharged","sedation_session",s.getId().toString(),"{}");
    return Map.of("ok",true,"session",s);
  }
}
