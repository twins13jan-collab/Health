package eg.gov.health.platform.clinicalquality;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Clinical quality, outcomes & risk (module 55). Clinical
 *  outcome, mortality, complication, readmission and
 *  hospital-acquired-infection indicators are managed;
 *  mortality and critical-case reviews and multidisciplinary
 *  meetings run; incidents, near misses and serious events are
 *  reported with root-cause analysis; improvement plans and
 *  corrective and preventive actions are verified for
 *  effectiveness; care pathways and protocols are audited for
 *  adherence, deviations and exceptions; patient-reported
 *  outcomes and experience are managed at service and facility
 *  level; and safety and quality indicators are cut by
 *  specialty and population with case-mix taken into account. */
@RestController @RequestMapping("/api/clinical-quality")
public class ClinicalQualityController {
  private final QualityIndicatorRepository indicators; private final MortalityReviewRepository reviews;
  private final ImprovementPlanRepository plans; private final AuditService audit;
  public ClinicalQualityController(QualityIndicatorRepository indicators, MortalityReviewRepository reviews,
                                   ImprovementPlanRepository plans, AuditService audit) {
    this.indicators=indicators; this.reviews=reviews; this.plans=plans; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }

  public record IndicatorBody(String name, String category, Double value, Boolean caseMixAdjusted) {}
  @PostMapping("/indicators")
  Map<String,Object> indicator(@RequestBody IndicatorBody r) {
    var p = principal();
    var i = indicators.save(new QualityIndicator(UUID.fromString(p.facilityId()), p.governorate(), r.name(), r.category(), r.value(),
      Boolean.TRUE.equals(r.caseMixAdjusted())));
    return Map.of("ok",true,"indicator",i,"comparable",QualityRules.comparable(i.isCaseMixAdjusted()),
      "note","an unadjusted rate is a number, not a comparison — raw rates punish whoever takes the hardest cases");
  }
  public record ReviewBody(String caseRef, Boolean reviewDone, Boolean preventable) {}
  @PostMapping("/mortality-reviews")
  Map<String,Object> review(@RequestBody ReviewBody r) {
    var p = principal();
    var rev = reviews.save(new MortalityReview(UUID.fromString(p.facilityId()), p.governorate(), r.caseRef(),
      Boolean.TRUE.equals(r.reviewDone()), r.preventable()));
    return Map.of("ok",true,"review",rev,"quality_closure_ready",rev.isReviewDone());
  }
  public record PlanBody(String title, Integer actionsTotal, Integer actionsDone, Boolean effectivenessVerified) {}
  @PostMapping("/improvement-plans")
  Map<String,Object> plan(@RequestBody PlanBody r) {
    var p = principal();
    var pl = plans.save(new ImprovementPlan(UUID.fromString(p.facilityId()), p.governorate(), r.title(),
      r.actionsTotal(), r.actionsDone(), Boolean.TRUE.equals(r.effectivenessVerified())));
    return Map.of("ok",true,"plan",pl);
  }
  @PostMapping("/improvement-plans/{id}/close")
  Map<String,Object> close(@PathVariable UUID id) {
    principal();
    var pl = plans.findById(id).orElseThrow(() -> BizException.notFound("plan_not_found","الخطة غير موجودة"));
    if (!QualityRules.planClosable(pl.getActionsTotal() == null ? 0 : pl.getActionsTotal(),
                                   pl.getActionsDone() == null ? 0 : pl.getActionsDone(), pl.isEffectivenessVerified()))
      throw BizException.conflict("not_closable","خطة تحسين لا تُغلق قبل اكتمال إجراءاتها والتحقق من فاعليتها — إجراءات منجزة جهد، لا تحسن مثبت");
    pl.close(); plans.save(pl);
    audit.record("quality.plan.closed","improvement_plan",id.toString(),"{}");
    return Map.of("ok",true,"plan",pl);
  }
}
