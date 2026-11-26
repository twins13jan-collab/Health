package eg.gov.health.platform.hta;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Health technology assessment (module 166). Drugs, devices,
 *  procedures and digital technologies are assessed across
 *  effectiveness, safety, cost-effectiveness, budget impact and
 *  ethical impact, with the evidence reviewed for quality and
 *  local applicability. Conflicts of interest are managed before
 *  appraisal, stakeholders participate, and the adoption or
 *  restriction recommendation is documented — with its strength
 *  capped by the quality of the evidence behind it. */
@RestController @RequestMapping("/api/hta")
public class HtaController {
  private final HtaAssessmentRepository assessments; private final HtaEvidenceItemRepository evidence;
  private final HtaPanelMemberRepository panel; private final HtaRecommendationRepository recommendations; private final AuditService audit;
  public HtaController(HtaAssessmentRepository assessments, HtaEvidenceItemRepository evidence, HtaPanelMemberRepository panel,
                       HtaRecommendationRepository recommendations, AuditService audit) {
    this.assessments=assessments; this.evidence=evidence; this.panel=panel; this.recommendations=recommendations; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private HtaAssessment assessment(UUID id) {
    return assessments.findById(id).orElseThrow(() -> BizException.notFound("assessment_not_found","التقييم غير موجود"));
  }

  public record AssessmentRequest(String technologyName, String techType) {}
  @PostMapping("/assessments")
  Map<String,Object> open(@RequestBody AssessmentRequest r) {
    var p = principal();
    var a = assessments.save(new HtaAssessment(UUID.fromString(p.facilityId()), p.governorate(), r.technologyName(), r.techType(), p.email()));
    audit.record("hta.assessment.opened","hta_assessment",a.getId().toString(),"{}");
    return Map.of("ok",true,"assessment",a);
  }
  public record EvidenceRequest(String source, String studyType, String qualityRating) {}
  @PostMapping("/assessments/{id}/evidence")
  Map<String,Object> evidence(@PathVariable UUID id, @RequestBody EvidenceRequest r) {
    var p = principal(); assessment(id);
    var e = evidence.save(new HtaEvidenceItem(UUID.fromString(p.facilityId()), p.governorate(), id, r.source(), r.studyType(), r.qualityRating()));
    return Map.of("ok",true,"evidence",e);
  }
  public record MemberRequest(String memberName, Boolean coiDeclared, String coiDetail) {}
  @PostMapping("/assessments/{id}/panel")
  Map<String,Object> member(@PathVariable UUID id, @RequestBody MemberRequest r) {
    var p = principal(); assessment(id);
    var m = panel.save(new HtaPanelMember(UUID.fromString(p.facilityId()), p.governorate(), id, r.memberName(), Boolean.TRUE.equals(r.coiDeclared()), r.coiDetail()));
    return Map.of("ok",true,"member",m);
  }
  public record AppraisalRequest(String effectiveness, String safety, String costEffectiveness, String budgetImpact,
                                 String ethicalImpact, String evidenceQuality, String applicability, Boolean stakeholdersConsulted) {}
  @PostMapping("/assessments/{id}/appraisal")
  Map<String,Object> appraise(@PathVariable UUID id, @RequestBody AppraisalRequest r) {
    principal(); var a = assessment(id);
    long members = panel.countByAssessmentId(id);
    long undeclared = panel.countByAssessmentIdAndCoiDeclaredFalse(id);
    if (!HtaRules.appraisalPermitted(members, undeclared, Boolean.TRUE.equals(r.stakeholdersConsulted())))
      throw BizException.conflict("appraisal_blocked","التقييم يتطلب لجنة معلنة تضارب المصالح بالكامل ومشاركة أصحاب مصلحة موثقة");
    a.appraise(r.effectiveness(), r.safety(), r.costEffectiveness(), r.budgetImpact(), r.ethicalImpact(),
      r.evidenceQuality(), r.applicability(), Boolean.TRUE.equals(r.stakeholdersConsulted()));
    assessments.save(a);
    audit.record("hta.appraisal.completed","hta_assessment",id.toString(),"{}");
    return Map.of("ok",true,"assessment",a);
  }
  public record RecommendationRequest(String recommendation, String conditions) {}
  @PostMapping("/assessments/{id}/recommendation")
  Map<String,Object> recommend(@PathVariable UUID id, @RequestBody RecommendationRequest r) {
    var p = principal(); var a = assessment(id);
    if (!"appraisal".equals(a.getStatus())) throw BizException.conflict("no_appraisal","لا توصية قبل اكتمال التقييم");
    if (!HtaRules.recommendationPermitted(r.recommendation(), a.getEvidenceQuality(), a.getEffectiveness(), a.getSafety()))
      throw BizException.conflict("recommendation_exceeds_evidence","التبني يتطلب أدلة فاعلية بجودة متوسطة على الأقل وسلامة مقبولة — الأدلة الضعيفة نتيجتها بحث فقط أو رفض");
    var rec = recommendations.save(new HtaRecommendation(UUID.fromString(p.facilityId()), p.governorate(), id, r.recommendation(), r.conditions(), p.email()));
    a.decide(); assessments.save(a);
    audit.record("hta.recommendation.issued","hta_recommendation",rec.getId().toString(),"{}");
    return Map.of("ok",true,"recommendation",rec);
  }
}
