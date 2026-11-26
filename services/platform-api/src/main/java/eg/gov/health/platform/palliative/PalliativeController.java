package eg.gov.health.platform.palliative;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.*; import java.util.*;
/** Palliative care, pain & end-of-life (cycle 247). The system records and
 *  coordinates — it never invents a patient's wish, never assumes a
 *  representative's authority, and never issues a directive on anyone's
 *  behalf. Symptom plans are physician-approved interventions with
 *  measured responses; document conflicts resolve by verification. */
@RestController @RequestMapping("/api/palliative")
public class PalliativeController {
  private final PalliativeAssessmentRepository assessments; private final SymptomPlanRepository symptomPlans;
  private final GoalsOfCareRepository goals; private final AdvanceCareDocumentRepository documents;
  private final BereavementSupportRepository bereavement; private final AuditService audit;
  public PalliativeController(PalliativeAssessmentRepository assessments, SymptomPlanRepository symptomPlans,
                              GoalsOfCareRepository goals, AdvanceCareDocumentRepository documents,
                              BereavementSupportRepository bereavement, AuditService audit) {
    this.assessments=assessments; this.symptomPlans=symptomPlans; this.goals=goals;
    this.documents=documents; this.bereavement=bereavement; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }

  public record AssessmentRequest(UUID patientId, String symptoms, Integer painScore, String functionalStatus, String needs, String assessedBy) {}
  @PostMapping("/assessments")
  Map<String,Object> assess(@RequestBody AssessmentRequest r) {
    var p = principal();
    if (r.patientId()==null) throw BizException.badRequest("invalid_assessment","المريض مطلوب");
    String band;
    try { band = PalliativeRules.painBand(r.painScore()); }
    catch (IllegalArgumentException ex) { throw BizException.badRequest("pain_out_of_range", ex.getMessage()); }
    var a = assessments.save(new PalliativeAssessment(UUID.fromString(p.facilityId()), p.governorate(), r.patientId(),
      r.symptoms(), r.painScore(), r.functionalStatus(), r.needs(), r.assessedBy()));
    audit.record("palliative.assessment.recorded","palliative_assessment",a.getId().toString(),"{}");
    return Map.of("ok",true,"assessment",a,"pain_band",band,
      "urgent_response_required", PalliativeRules.urgentResponseRequired(r.painScore()));
  }

  public record SymptomPlanRequest(UUID patientId, String symptom, String goal, String intervention, String responsible,
                                   Integer painBefore, Integer reviewDueHours) {}
  @PostMapping("/symptom-plans")
  Map<String,Object> symptomPlan(@RequestBody SymptomPlanRequest r) {
    var p = principal();
    if (r.patientId()==null||r.symptom()==null) throw BizException.badRequest("invalid_plan","المريض والعرَض مطلوبان");
    var due = r.reviewDueHours()==null ? null : OffsetDateTime.now().plusHours(r.reviewDueHours());
    var sp = symptomPlans.save(new SymptomPlan(UUID.fromString(p.facilityId()), p.governorate(), r.patientId(),
      r.symptom(), r.goal(), r.intervention(), r.responsible(), r.painBefore(), due));
    audit.record("palliative.symptom_plan.created","symptom_plan",sp.getId().toString(),"{}");
    return Map.of("ok",true,"symptom_plan",sp);
  }
  public record ResponseRequest(Integer painAfter, String sideEffects) {}
  @PostMapping("/symptom-plans/{id}/response")
  Map<String,Object> respond(@PathVariable UUID id, @RequestBody ResponseRequest r) {
    var sp = symptomPlans.findById(id).orElseThrow(() -> BizException.notFound("plan_not_found","خطة الأعراض غير موجودة"));
    if (!"active".equals(sp.getStatus())) throw BizException.conflict("plan_not_active","الخطة ليست نشطة");
    if (r.painAfter()==null) throw BizException.badRequest("pain_after_required","قياس الألم بعد التدخل مطلوب لتوثيق الاستجابة");
    sp.respond(r.painAfter(), r.sideEffects()); symptomPlans.save(sp);
    audit.record("palliative.symptom_plan.responded","symptom_plan",id.toString(),"{}");
    Object relief = sp.getPainBefore()==null ? "" : PalliativeRules.reliefDelta(sp.getPainBefore(), r.painAfter());
    Object achieved = sp.getPainBefore()==null ? "" : PalliativeRules.reliefAchieved(sp.getPainBefore(), r.painAfter());
    return Map.of("ok",true,"symptom_plan",sp,"relief_delta",relief,"relief_achieved",achieved);
  }

  public record GoalsRequest(UUID patientId, String preferences, String representativeName, Boolean representativeVerified,
                             String documentationSource, LocalDate reviewDate) {}
  @PostMapping("/goals")
  Map<String,Object> goals(@RequestBody GoalsRequest r) {
    var p = principal();
    if (r.patientId()==null||r.preferences()==null||r.documentationSource()==null)
      throw BizException.badRequest("invalid_goals","المريض والتفضيلات ومصدر التوثيق مطلوبة — الرغبات تُسجل بمصدرها دائمًا (دورة 247)");
    // The system never assumes a representative's authority.
    if (!PalliativeRules.representativeAcceptable(r.representativeName(), Boolean.TRUE.equals(r.representativeVerified()), r.documentationSource()))
      throw BizException.badRequest("representative_not_verified","تمثيل قانوني غير موثق: لا يُفترض ممثل للمريض — يلزم تحقق قانوني ومصدر معتمد");
    var g = goals.save(new GoalsOfCare(UUID.fromString(p.facilityId()), p.governorate(), r.patientId(), r.preferences(),
      r.representativeName(), Boolean.TRUE.equals(r.representativeVerified()), r.documentationSource(), r.reviewDate()));
    audit.record("palliative.goals.recorded","goals_of_care",g.getId().toString(),"{}");
    return Map.of("ok",true,"goals",g);
  }
  @PostMapping("/goals/{id}/disagreement")
  Map<String,Object> disagreement(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var g = goals.findById(id).orElseThrow(() -> BizException.notFound("goals_not_found","سجل أهداف الرعاية غير موجود"));
    String parties = body.get("parties"), basis = body.get("legalBasis");
    if (parties==null||basis==null) throw BizException.badRequest("disagreement_detail_required","اختلاف الرغبات يتطلب توثيق الأطراف والأساس القانوني (دورة 247)");
    g.contest(parties, basis); goals.save(g);
    audit.record("palliative.goals.contested","goals_of_care",id.toString(),"{}");
    return Map.of("ok",true,"goals",g,"escalation_required",true,
      "note","patient wish prevails unless a verified legal basis says otherwise — escalate to ethics/legal review");
  }

  public record DocumentRequest(UUID patientId, String documentType, Integer version, Boolean legalVerified, String scope) {}
  @PostMapping("/documents")
  Map<String,Object> document(@RequestBody DocumentRequest r) {
    var p = principal();
    if (r.patientId()==null||r.documentType()==null) throw BizException.badRequest("invalid_document","المريض ونوع الوثيقة مطلوبان");
    var active = documents.findByPatientIdAndStatus(r.patientId(), "active").stream()
      .filter(d -> d.getDocumentType().equals(r.documentType())).findFirst().orElse(null);
    boolean newVerified = Boolean.TRUE.equals(r.legalVerified());
    if (active != null && !PalliativeRules.maySupersede(active.isLegalVerified(), newVerified))
      throw BizException.conflict("unverified_cannot_supersede","وثيقة غير موثقة قانونيًا لا تلغي وثيقة موثقة — راجع المصدر والصلاحية (دورة 247)");
    var d = documents.save(new AdvanceCareDocument(UUID.fromString(p.facilityId()), p.governorate(), r.patientId(),
      r.documentType(), r.version()==null?1:r.version(), newVerified, r.scope()));
    if (active != null) { active.supersede(); documents.save(active); }
    audit.record("palliative.document.recorded","advance_care_document",d.getId().toString(),"{}");
    return Map.of("ok",true,"document",d,"superseded_previous", active!=null);
  }

  public record BereavementRequest(UUID patientId, String familyContact, String request, String referral) {}
  @PostMapping("/bereavement")
  Map<String,Object> bereavement(@RequestBody BereavementRequest r) {
    var p = principal();
    if (r.patientId()==null||r.request()==null) throw BizException.badRequest("invalid_bereavement","المريض وطلب الدعم مطلوبان");
    var b = bereavement.save(new BereavementSupport(UUID.fromString(p.facilityId()), p.governorate(), r.patientId(),
      r.familyContact(), r.request(), r.referral()));
    audit.record("palliative.bereavement.requested","bereavement_support",b.getId().toString(),"{}");
    return Map.of("ok",true,"bereavement",b);
  }

  @GetMapping("/assessments")
  Map<String,Object> byPatient(@RequestParam UUID patientId) {
    var rows = assessments.findByPatientIdOrderByAssessedAtDesc(patientId);
    return Map.of("ok",true,"assessments",rows,"count",rows.size());
  }
}
