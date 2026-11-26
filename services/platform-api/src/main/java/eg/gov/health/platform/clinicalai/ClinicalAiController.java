package eg.gov.health.platform.clinicalai;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal; import java.util.*;
/** Clinical AI registry, validation, inference & human verification
 *  (cycle 233). Models are registered with provenance and limits,
 *  validated on a described population, and approved for clinical use
 *  by a competent committee — never by the model owner alone. Every
 *  inference shows its model version, confidence and limits, abstains
 *  on incomplete or out-of-scope inputs, and takes effect only
 *  through a recorded human decision: the practitioner holds the
 *  final clinical decision. Drift suspends; incidents are
 *  investigated; a suspended model can be re-approved after review.
 *  A model outage never blocks the ordinary clinical path. */
@RestController @RequestMapping("/api/clinical-ai")
public class ClinicalAiController {
  private final AiModelRegistryRepository models; private final AiModelValidationRepository validations;
  private final AiInferenceRepository inferences; private final AiHumanReviewRepository reviews;
  private final AiModelIncidentRepository incidents; private final AuditService audit;
  public ClinicalAiController(AiModelRegistryRepository models, AiModelValidationRepository validations,
                              AiInferenceRepository inferences, AiHumanReviewRepository reviews,
                              AiModelIncidentRepository incidents, AuditService audit) {
    this.models=models; this.validations=validations; this.inferences=inferences; this.reviews=reviews;
    this.incidents=incidents; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private AiModelRegistry model(UUID id) {
    return models.findById(id).orElseThrow(() -> BizException.notFound("model_not_found","النموذج غير موجود"));
  }

  public record ModelRequest(String name, String version, String vendor, String purpose, String riskClass, String trainingData, String limitations) {}
  @PostMapping("/models")
  Map<String,Object> register(@RequestBody ModelRequest r) {
    var p = principal();
    if (r.name()==null||r.version()==null||r.limitations()==null)
      throw BizException.badRequest("registry_fields","سجل النموذج يتطلب الاسم والإصدار والقيود المعلنة");
    var m = models.save(new AiModelRegistry(UUID.fromString(p.facilityId()), p.governorate(), r.name(), r.version(),
      r.vendor(), r.purpose(), r.riskClass(), r.trainingData(), r.limitations(), p.email()));
    audit.record("ai.model.registered","ai_model",m.getId().toString(),"{}");
    return Map.of("ok",true,"model",m,"note","the owner manages versions but never approves clinical use alone");
  }
  public record ValidationRequest(String datasetDescription, String population, String performance, String biasAssessment, Boolean approved, String committee) {}
  @PostMapping("/models/{id}/validations")
  Map<String,Object> validate(@PathVariable UUID id, @RequestBody ValidationRequest r) {
    var p = principal(); var m = model(id);
    boolean approved = Boolean.TRUE.equals(r.approved());
    var v = validations.save(new AiModelValidation(m.getFacilityId(), p.governorate(), id, r.datasetDescription(),
      r.population(), r.performance(), r.biasAssessment(), approved, r.committee()));
    if (approved) { m.markValidated(); models.save(m); }
    audit.record("ai.model.validated","ai_model_validation",v.getId().toString(),"{}");
    return Map.of("ok",true,"validation",v,"model_status",m.getStatus());
  }
  @PostMapping("/models/{id}/approve")
  Map<String,Object> approve(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var p = principal(); var m = model(id);
    if (!AiRules.mayApproveClinicalUse(p.role()))
      throw BizException.forbidden("اللجنة المختصة تعتمد الاستخدام السريري وفق المجال — لا مسؤول النموذج منفردًا");
    if (p.email().equals(m.getOwner()))
      throw BizException.forbidden("مسؤول النموذج يدير الإصدارات ولا يعتمد الاستخدام السريري لنموذجه");
    if (!"validated".equals(m.getStatus()))
      throw BizException.conflict("not_validated","لا اعتماد سريري قبل تحقق موثق على سكان محددين");
    if (body.get("scope")==null) throw BizException.badRequest("scope_required","الاعتماد لمجال استخدام محدد");
    m.approve(body.get("scope")); models.save(m);
    audit.record("ai.model.approved","ai_model",id.toString(),"{}");
    return Map.of("ok",true,"model",m);
  }
  public record InferenceRequest(String patientRef, String inputsSummary, Boolean inputsComplete, Boolean inScope, String result, BigDecimal confidence) {}
  @PostMapping("/models/{id}/inferences")
  Map<String,Object> infer(@PathVariable UUID id, @RequestBody InferenceRequest r) {
    var p = principal(); var m = model(id);
    if (!AiRules.inferable(m.getStatus(), m.isCommitteeApproved()))
      throw BizException.conflict("model_not_approved","لا استدلال سريري من نموذج غير معتمد أو معلّق — والمسار السريري الاعتيادي لا يتعطل أبدًا");
    var inf = inferences.save(new AiInference(m.getFacilityId(), p.governorate(), id, m.getVersion(), r.patientRef(),
      r.inputsSummary(), Boolean.TRUE.equals(r.inputsComplete()), Boolean.TRUE.equals(r.inScope()), r.result(), r.confidence(), p.email()));
    audit.record("ai.inference.created","ai_inference",inf.getId().toString(),"{}");
    return Map.of("ok",true,"inference",inf,"model_version",m.getVersion(),"risk_class",m.getRiskClass(),
      "requires_human_review",true,"note","advisory only — no automated high-risk decision takes effect without human approval");
  }
  public record ReviewRequest(String decision, String reason, String finalDecision) {}
  @PostMapping("/inferences/{id}/review")
  Map<String,Object> review(@PathVariable UUID id, @RequestBody ReviewRequest r) {
    var p = principal();
    var inf = inferences.findById(id).orElseThrow(() -> BizException.notFound("inference_not_found","الاستدلال غير موجود"));
    if (inf.isAbstained()) throw BizException.conflict("abstained","استدلال ممتنع لمدخلات ناقصة أو خارج النطاق لا يراجع كتوصية");
    if (!Set.of("accepted","rejected","modified").contains(r.decision())||r.reason()==null)
      throw BizException.badRequest("review_fields","قرار المراجعة (قبول/رفض/تعديل) وسببه مطلوبان");
    var rev = reviews.save(new AiHumanReview(infFacility(inf), p.governorate(), id, p.email(), r.decision(), r.reason(), r.finalDecision()));
    inf.markReviewed(); inferences.save(inf);
    audit.record("ai.inference.reviewed","ai_human_review",rev.getId().toString(),"{}");
    return Map.of("ok",true,"review",rev,"final_clinical_decision_holder",p.email());
  }
  private UUID infFacility(AiInference i) { return i.getFacilityId(); }
  /** Applying an inference into care requires the recorded human decision. */
  @PostMapping("/inferences/{id}/apply")
  Map<String,Object> apply(@PathVariable UUID id) {
    var inf = inferences.findById(id).orElseThrow(() -> BizException.notFound("inference_not_found","الاستدلال غير موجود"));
    String decision = reviews.findByInferenceIdOrderByCreatedAtDesc(id).stream().findFirst().map(AiHumanReview::getDecision).orElse(null);
    if (!AiRules.applicable(decision))
      throw BizException.conflict("no_human_approval","لا يصدر قرار آلي عالي المخاطر دون الإشراف والاعتماد البشري اللازمين");
    inf.apply(); inferences.save(inf);
    audit.record("ai.inference.applied","ai_inference",id.toString(),"{}");
    return Map.of("ok",true,"inference",inf,"applied_under_human_decision",decision);
  }
  public record IncidentRequest(String kind, String description, BigDecimal driftMetric) {}
  @PostMapping("/models/{id}/incidents")
  Map<String,Object> incident(@PathVariable UUID id, @RequestBody IncidentRequest r) {
    var p = principal(); var m = model(id);
    boolean suspend = "drift".equals(r.kind()) && AiRules.driftSuspends(r.driftMetric());
    var inc = incidents.save(new AiModelIncident(m.getFacilityId(), p.governorate(), id, r.kind(),
      r.description(), r.driftMetric(), suspend ? "suspended" : "monitoring"));
    if (suspend) { m.suspend(); models.save(m); }
    audit.record("ai.model.incident","ai_model_incident",inc.getId().toString(),"{}");
    return Map.of("ok",true,"incident",inc,"model_status",m.getStatus(),
      "note", suspend ? "performance drift at/above threshold — model suspended pending investigation"
                      : "recorded; monitoring continues");
  }
  @PostMapping("/models/{id}/reapprove")
  Map<String,Object> reapprove(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var p = principal(); var m = model(id);
    if (!AiRules.mayApproveClinicalUse(p.role()))
      throw BizException.forbidden("إعادة الاعتماد بعد التعليق للجنة المختصة بعد تحقيق موثق");
    if (body.get("investigation")==null) throw BizException.badRequest("investigation_required","التحقيق في الحادث/الانحراف شرط إعادة الاعتماد");
    m.reapprove(); models.save(m);
    incidents.findByModelIdOrderByCreatedAtDesc(id).stream().filter(i -> "suspended".equals(i.getStatus()))
      .forEach(i -> { i.close(body.get("investigation"), "reapproved"); incidents.save(i); });
    audit.record("ai.model.reapproved","ai_model",id.toString(),"{}");
    return Map.of("ok",true,"model",m);
  }
  @GetMapping("/models/{id}")
  Map<String,Object> get(@PathVariable UUID id) {
    var m = model(id);
    return Map.of("ok",true,"model",m,"validations",validations.findByModelIdOrderByCreatedAtDesc(id),
      "incidents",incidents.findByModelIdOrderByCreatedAtDesc(id));
  }
}
