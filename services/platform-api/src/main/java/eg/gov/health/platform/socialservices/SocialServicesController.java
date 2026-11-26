package eg.gov.health.platform.socialservices;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.*; import java.util.*;
/** Social & psychological services (module 99). Social
 *  assessments and support needs are managed; referrals reach
 *  psychologists and social specialists; crisis risk is assessed
 *  with safety pathways; support plans are followed up; and
 *  children and vulnerable adults are protected through
 *  mandatory safeguarding referral. The social record stays
 *  behind role walls — it is not front-desk material. */
@RestController @RequestMapping("/api/social-services")
public class SocialServicesController {
  private final SocialAssessmentRepository assessments; private final SupportPlanRepository plans;
  private final SafeguardingReferralRepository referrals; private final AuditService audit;
  public SocialServicesController(SocialAssessmentRepository assessments, SupportPlanRepository plans,
                                  SafeguardingReferralRepository referrals, AuditService audit) {
    this.assessments=assessments; this.plans=plans; this.referrals=referrals; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    if (Set.of("receptionist").contains(p.role())) throw BizException.forbidden("السجل الاجتماعي من أكثر سجلات المنشأة حساسية — ليس من مادة مكتب الاستقبال");
    return p;
  }
  private SocialAssessment assessment(UUID id) {
    return assessments.findById(id).orElseThrow(() -> BizException.notFound("assessment_not_found","التقييم غير موجود"));
  }

  public record AssessRequest(String patientRef, String supportNeeds, String riskLevel, String safeguardingConcern) {}
  @PostMapping("/assessments")
  Map<String,Object> assess(@RequestBody AssessRequest r) {
    var p = principal();
    var a = assessments.save(new SocialAssessment(UUID.fromString(p.facilityId()), p.governorate(), r.patientRef(), r.supportNeeds(),
      r.riskLevel(), r.safeguardingConcern(), p.email()));
    audit.record("social.assessed","social_assessment",a.getId().toString(),"{}");
    return Map.of("ok",true,"assessment",a,
      "safety_plan_required",SocialRules.safetyPlanRequired(r.riskLevel()),
      "safeguarding_referral_required",SocialRules.safeguardingReferralRequired(r.safeguardingConcern()));
  }
  @PostMapping("/assessments/{id}/safety-plan")
  Map<String,Object> safetyPlan(@PathVariable UUID id) {
    principal(); var a = assessment(id);
    a.createSafetyPlan(); assessments.save(a);
    audit.record("social.safety_plan.created","social_assessment",id.toString(),"{}");
    return Map.of("ok",true,"assessment",a);
  }
  public record ReferralRequest(String referredTo) {}
  @PostMapping("/assessments/{id}/safeguarding-referral")
  Map<String,Object> safeguarding(@PathVariable UUID id, @RequestBody ReferralRequest r) {
    var p = principal(); var a = assessment(id);
    var ref = referrals.save(new SafeguardingReferral(UUID.fromString(p.facilityId()), p.governorate(), id, a.getSafeguardingConcern(), r.referredTo()));
    audit.record("social.safeguarding.referred","safeguarding_referral",ref.getId().toString(),"{}");
    return Map.of("ok",true,"referral",ref);
  }
  public record PlanRequest(String actions, LocalDate followUpDate) {}
  @PostMapping("/assessments/{id}/support-plans")
  Map<String,Object> plan(@PathVariable UUID id, @RequestBody PlanRequest r) {
    var p = principal(); assessment(id);
    var pl = plans.save(new SupportPlan(UUID.fromString(p.facilityId()), p.governorate(), id, r.actions(), r.followUpDate()));
    return Map.of("ok",true,"plan",pl);
  }
  @PostMapping("/support-plans/{pid}/close")
  Map<String,Object> closePlan(@PathVariable UUID pid) {
    principal();
    var pl = plans.findById(pid).orElseThrow(() -> BizException.notFound("plan_not_found","الخطة غير موجودة"));
    var a = assessments.findById(pl.getAssessmentId()).orElseThrow(() -> BizException.notFound("assessment_not_found","التقييم غير موجود"));
    boolean referralMade = referrals.existsByAssessmentId(a.getId());
    if (!SocialRules.planClosable(a.getRiskLevel(), a.isSafetyPlanCreated(), a.getSafeguardingConcern(), referralMade))
      throw BizException.conflict("closure_blocked","خطة الدعم لا تُغلق وخطر الأزمة بلا خطة سلامة أو قلق حماية بلا إحالة للجهة المختصة");
    pl.close(); plans.save(pl);
    return Map.of("ok",true,"plan",pl);
  }
}
