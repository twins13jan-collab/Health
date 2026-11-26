package eg.gov.health.platform.nutrition;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Clinical nutrition, enteral & parenteral feeding (cycle 251).
 *  Dietitian proposes, physician approves, pharmacist reviews parenteral
 *  orders before they start, nursing records what was actually given —
 *  and the prescribed-vs-given gap stays visible with its reason.
 *  Substitutions are approved changes, never automatic. */
@RestController @RequestMapping("/api/nutrition")
public class NutritionController {
  private final NutritionAssessmentRepository assessments; private final NutritionPlanRepository plans;
  private final FeedOrderRepository orders; private final AdministrationRecordRepository administrations;
  private final NutritionComplicationRepository complications; private final AuditService audit;
  public NutritionController(NutritionAssessmentRepository assessments, NutritionPlanRepository plans,
                             FeedOrderRepository orders, AdministrationRecordRepository administrations,
                             NutritionComplicationRepository complications, AuditService audit) {
    this.assessments=assessments; this.plans=plans; this.orders=orders;
    this.administrations=administrations; this.complications=complications; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private NutritionPlan plan(UUID id) {
    return plans.findById(id).orElseThrow(() -> BizException.notFound("plan_not_found","خطة التغذية غير موجودة"));
  }
  private FeedOrder order(UUID id) {
    return orders.findById(id).orElseThrow(() -> BizException.notFound("order_not_found","أمر التغذية غير موجود"));
  }

  public record AssessmentRequest(UUID patientId, Double weightKg, Double heightCm, Double weightLossPercent, String assessedBy) {}
  @PostMapping("/assessments")
  Map<String,Object> assess(@RequestBody AssessmentRequest r) {
    var p = principal();
    if (r.patientId()==null||r.weightKg()==null||r.heightCm()==null)
      throw BizException.badRequest("invalid_assessment","المريض والوزن والطول مطلوبة لحساب مؤشر الكتلة والمخاطر");
    NutritionAssessment a;
    try {
      a = assessments.save(new NutritionAssessment(UUID.fromString(p.facilityId()), p.governorate(), r.patientId(),
        r.weightKg(), r.heightCm(), r.weightLossPercent(), r.assessedBy()));
    } catch (IllegalArgumentException ex) { throw BizException.badRequest("invalid_measurement", ex.getMessage()); }
    audit.record("nutrition.screened","nutrition_assessment",a.getId().toString(),"{}");
    int[] range = NutritionRules.needsRangeKcal(r.weightKg());
    return Map.of("ok",true,"assessment",a,"bmi",a.getBmi(),"risk_level",a.getRiskLevel(),
      "suggested_needs_kcal", Map.of("min",range[0],"max",range[1]));
  }

  public record PlanRequest(UUID patientId, UUID assessmentId, Integer estimatedNeedsKcal, String route, String goal, String restrictions, String proposedBy) {}
  @PostMapping("/plans")
  Map<String,Object> plan(@RequestBody PlanRequest r) {
    var p = principal();
    if (r.patientId()==null||r.route()==null) throw BizException.badRequest("invalid_plan","المريض وطريق التغذية مطلوبان");
    Double weight = null;
    if (r.assessmentId()!=null) {
      var a = assessments.findById(r.assessmentId()).orElseThrow(() -> BizException.notFound("assessment_not_found","التقييم غير موجود"));
      weight = a.getWeightKg();
    }
    var pl = plans.save(new NutritionPlan(UUID.fromString(p.facilityId()), p.governorate(), r.patientId(), r.assessmentId(),
      r.estimatedNeedsKcal(), r.route(), r.goal(), r.restrictions(), r.proposedBy()));
    audit.record("nutrition.plan.proposed","nutrition_plan",pl.getId().toString(),"{}");
    return Map.of("ok",true,"plan",pl,
      "needs_out_of_range", weight!=null && NutritionRules.needsOutOfRange(r.estimatedNeedsKcal(), weight),
      "note","dietitian proposes — a physician must approve before any order");
  }
  @PostMapping("/plans/{id}/approve")
  Map<String,Object> approve(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var pl = plan(id);
    String by = body.get("approvedBy");
    if (by==null||by.isBlank()) throw BizException.badRequest("approver_required","اعتماد الطبيب للوصفة مطلوب (دورة 251)");
    if (!"proposed".equals(pl.getStatus())) throw BizException.conflict("not_proposed","الخطة ليست مقترحة");
    pl.approve(by); plans.save(pl);
    audit.record("nutrition.plan.approved","nutrition_plan",id.toString(),"{}");
    return Map.of("ok",true,"plan",pl);
  }

  public record OrderRequest(String kind, String formula, String components, String routeDetail, Double rateMlHr) {}
  @PostMapping("/plans/{id}/orders")
  Map<String,Object> order(@PathVariable UUID id, @RequestBody OrderRequest r) {
    var p = principal(); var pl = plan(id);
    if (!NutritionRules.ordersAllowed(pl.getStatus()))
      throw BizException.conflict("plan_not_approved","الأوامر تتطلب خطة معتمدة من الطبيب");
    if (r.kind()==null||!(r.kind().equals("enteral")||r.kind().equals("parenteral")))
      throw BizException.badRequest("invalid_kind","نوع التغذية: معوية أو وريدية");
    var o = orders.save(new FeedOrder(pl.getFacilityId(), p.governorate(), id, r.kind(), r.formula(), r.components(), r.routeDetail(), r.rateMlHr()));
    if ("approved".equals(pl.getStatus())) { pl.setStatus("active"); plans.save(pl); }
    audit.record("nutrition.order.created","feed_order",o.getId().toString(),"{}");
    return Map.of("ok",true,"order",o);
  }
  @PostMapping("/orders/{id}/pharmacist-review")
  Map<String,Object> review(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var o = order(id);
    String by = body.get("reviewedBy");
    if (by==null||by.isBlank()) throw BizException.badRequest("reviewer_required","مراجعة الصيدلي للتوافق والتحضير مطلوبة");
    o.review(by); orders.save(o);
    audit.record("nutrition.order.pharmacist_reviewed","feed_order",id.toString(),"{}");
    return Map.of("ok",true,"order",o);
  }
  @PostMapping("/orders/{id}/start")
  Map<String,Object> start(@PathVariable UUID id) {
    var o = order(id);
    if (!"ordered".equals(o.getStatus())) throw BizException.conflict("not_ordered","الأمر ليس في حالة طلب");
    // Parenteral nutrition never starts before pharmacist review.
    if (!NutritionRules.mayStart(o.getKind(), o.isPharmacistReviewed()))
      throw BizException.conflict("pharmacist_review_required","التغذية الوريدية لا تبدأ قبل مراجعة الصيدلي للتوافق والتحضير (دورة 251)");
    o.start(); orders.save(o);
    audit.record("nutrition.feed.started","feed_order",id.toString(),"{}");
    return Map.of("ok",true,"order",o);
  }
  /** A substitution is an approved change — never automatic. */
  @PostMapping("/orders/{id}/substitute")
  Map<String,Object> substitute(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var o = order(id);
    String components = body.get("components"), approvedBy = body.get("approvedBy");
    if (components==null||approvedBy==null||approvedBy.isBlank())
      throw BizException.badRequest("substitution_needs_approval","مكون غير متاح لا يُستبدل تلقائيًا — الاستبدال تغيير معتمد بموافقة موثقة");
    o.substitute(components); orders.save(o);
    audit.record("nutrition.order.substituted","feed_order",id.toString(),"{}");
    return Map.of("ok",true,"order",o,"approved_by",approvedBy);
  }

  public record AdministrationRequest(Double prescribedMl, Double actualMl, Double remainingMl, Boolean stopped, String stopReason, String recordedBy) {}
  @PostMapping("/orders/{id}/administrations")
  Map<String,Object> administer(@PathVariable UUID id, @RequestBody AdministrationRequest r) {
    var p = principal(); var o = order(id);
    if (r.prescribedMl()==null||r.actualMl()==null) throw BizException.badRequest("invalid_administration","الموصوف والمنفذ فعليًا مطلوبان — الفصل بينهما أساس الدورة");
    boolean stopped = Boolean.TRUE.equals(r.stopped());
    // The gap is shown with its reason — never silently absorbed.
    if (NutritionRules.gapNeedsReason(r.prescribedMl(), r.actualMl(), stopped, r.stopReason()))
      throw BizException.badRequest("gap_reason_required","فرق بين الموصوف والمنفذ أو توقف مبكر يتطلب سببًا موثقًا (دورة 251)");
    var rec = administrations.save(new AdministrationRecord(o.getFacilityId(), p.governorate(), id, r.prescribedMl(),
      r.actualMl(), r.remainingMl()==null?0:r.remainingMl(), stopped, r.stopReason(), r.recordedBy()));
    audit.record("nutrition.administration.recorded","administration_record",rec.getId().toString(),"{}");
    return Map.of("ok",true,"record",rec,"variance_ml", NutritionRules.varianceMl(r.prescribedMl(), r.actualMl()));
  }
  @GetMapping("/orders/{id}/gap")
  Map<String,Object> gap(@PathVariable UUID id) {
    order(id);
    var rows = administrations.findByOrderIdOrderByCreatedAtAsc(id);
    double prescribed = rows.stream().mapToDouble(AdministrationRecord::getPrescribedMl).sum();
    double actual = rows.stream().mapToDouble(AdministrationRecord::getActualMl).sum();
    return Map.of("ok",true,"records",rows.size(),"total_prescribed_ml",prescribed,"total_actual_ml",actual,
      "total_variance_ml", NutritionRules.varianceMl(prescribed, actual),
      "note","the gap is reported as recorded — administration history is never rewritten");
  }

  public record ComplicationRequest(UUID patientId, UUID orderId, String type, String severity, String actionTaken) {}
  @PostMapping("/complications")
  Map<String,Object> complication(@RequestBody ComplicationRequest r) {
    var p = principal();
    if (r.patientId()==null||r.type()==null) throw BizException.badRequest("invalid_complication","المريض ونوع المضاعفة مطلوبان");
    var comp = complications.save(new NutritionComplication(UUID.fromString(p.facilityId()), p.governorate(), r.patientId(),
      r.orderId(), r.type(), r.severity(), r.actionTaken()));
    boolean hold = NutritionRules.holdsFeed(r.type());
    if (hold && r.orderId()!=null) {
      var o = order(r.orderId());
      if ("started".equals(o.getStatus())) { o.hold(); orders.save(o); }
    }
    audit.record("nutrition.complication.reported","nutrition_complication",comp.getId().toString(),"{}");
    return Map.of("ok",true,"complication",comp,"feed_held",hold,
      "protocol_action", hold ? "feed held — follow the approved intolerance/aspiration protocol and reassess" : "monitor and review with the team");
  }

  @GetMapping("/plans")
  Map<String,Object> byPatient(@RequestParam UUID patientId) {
    var rows = plans.findByPatientIdOrderByCreatedAtDesc(patientId);
    return Map.of("ok",true,"plans",rows,"count",rows.size());
  }
}
