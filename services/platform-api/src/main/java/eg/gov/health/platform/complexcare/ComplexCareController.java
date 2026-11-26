package eg.gov.health.platform.complexcare;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.math.*; import java.time.*; import java.util.*;
/** Complex health cases (module 142). Multi-party care plans with
 *  a named care coordinator and defined team responsibilities,
 *  managed transitions between hospital, home and rehabilitation,
 *  home equipment and supplies, and an individual emergency plan
 *  per patient. A plan owned by everyone is owned by no one: no
 *  activation without a coordinator and a responsible team, no
 *  transition without a documented handover and arranged
 *  equipment, and no silent closure — continuity and quality of
 *  life are measured. */
@RestController @RequestMapping("/api/complex-care")
public class ComplexCareController {
  private final ComplexCarePlanRepository plans; private final CareTeamMemberRepository team;
  private final CareTransitionRepository transitions; private final HomeEquipmentOrderRepository equipment;
  private final IndividualEmergencyPlanRepository emergencyPlans; private final AuditService audit;
  public ComplexCareController(ComplexCarePlanRepository plans, CareTeamMemberRepository team, CareTransitionRepository transitions,
                               HomeEquipmentOrderRepository equipment, IndividualEmergencyPlanRepository emergencyPlans, AuditService audit) {
    this.plans=plans; this.team=team; this.transitions=transitions; this.equipment=equipment; this.emergencyPlans=emergencyPlans; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private ComplexCarePlan plan(UUID id) {
    return plans.findById(id).orElseThrow(() -> BizException.notFound("plan_not_found","خطة الرعاية غير موجودة"));
  }

  public record PlanRequest(String patientRef, String coordinator, String conditionsSummary) {}
  @PostMapping("/plans")
  Map<String,Object> create(@RequestBody PlanRequest r) {
    var p = principal();
    var pl = plans.save(new ComplexCarePlan(UUID.fromString(p.facilityId()), p.governorate(), r.patientRef(), r.coordinator(), r.conditionsSummary()));
    audit.record("complex.plan.created","complex_care_plan",pl.getId().toString(),"{}");
    return Map.of("ok",true,"plan",pl);
  }
  public record MemberRequest(String memberName, String memberRole, String responsibility) {}
  @PostMapping("/plans/{id}/team")
  Map<String,Object> member(@PathVariable UUID id, @RequestBody MemberRequest r) {
    var p = principal(); var pl = plan(id);
    var m = team.save(new CareTeamMember(UUID.fromString(p.facilityId()), p.governorate(), id, r.memberName(), r.memberRole(), r.responsibility()));
    return Map.of("ok",true,"member",m);
  }
  @PostMapping("/plans/{id}/activate")
  Map<String,Object> activate(@PathVariable UUID id) {
    principal(); var pl = plan(id);
    long responsible = team.countByPlanIdAndResponsibilityIsNotNull(id);
    if (!ComplexCareRules.planActivatable(pl.getCoordinator(), responsible))
      throw BizException.conflict("plan_incomplete","التفعيل يتطلب منسق رعاية مسمى وعضو فريق واحدًا على الأقل بمسؤولية محددة");
    pl.activate(); plans.save(pl);
    audit.record("complex.plan.activated","complex_care_plan",id.toString(),"{}");
    return Map.of("ok",true,"plan",pl);
  }
  public record TransitionRequest(String fromSetting, String toSetting, Boolean handoverDocumented, Boolean equipmentNeeded, Boolean equipmentArranged) {}
  @PostMapping("/plans/{id}/transitions")
  Map<String,Object> transition(@PathVariable UUID id, @RequestBody TransitionRequest r) {
    var p = principal(); var pl = plan(id);
    if (!"active".equals(pl.getStatus())) throw BizException.conflict("plan_not_active","الانتقالات تتم على خطة نشطة");
    if (!ComplexCareRules.transitionPermitted(Boolean.TRUE.equals(r.handoverDocumented()), Boolean.TRUE.equals(r.equipmentNeeded()), Boolean.TRUE.equals(r.equipmentArranged())))
      throw BizException.conflict("transition_blocked","الانتقال يتطلب تسليمًا موثقًا وتجهيز الأجهزة المنزلية المطلوبة قبل التحرك");
    var t = transitions.save(new CareTransition(UUID.fromString(p.facilityId()), p.governorate(), id, r.fromSetting(), r.toSetting(),
      Boolean.TRUE.equals(r.handoverDocumented()), Boolean.TRUE.equals(r.equipmentNeeded()), Boolean.TRUE.equals(r.equipmentArranged())));
    audit.record("complex.transition.done","care_transition",t.getId().toString(),"{}");
    return Map.of("ok",true,"transition",t);
  }
  public record EquipmentRequest(String equipment, String supplier) {}
  @PostMapping("/plans/{id}/equipment")
  Map<String,Object> order(@PathVariable UUID id, @RequestBody EquipmentRequest r) {
    var p = principal(); plan(id);
    var o = equipment.save(new HomeEquipmentOrder(UUID.fromString(p.facilityId()), p.governorate(), id, r.equipment(), r.supplier()));
    return Map.of("ok",true,"order",o);
  }
  @PostMapping("/equipment/{oid}/deliver")
  Map<String,Object> deliver(@PathVariable UUID oid) {
    principal();
    var o = equipment.findById(oid).orElseThrow(() -> BizException.notFound("equipment_not_found","طلب الجهاز غير موجود"));
    o.deliver(); equipment.save(o);
    return Map.of("ok",true,"order",o);
  }
  public record EmergencyPlanRequest(String triggers, String actions, String contacts, LocalDate reviewedAt) {}
  @PostMapping("/plans/{id}/emergency-plan")
  Map<String,Object> emergency(@PathVariable UUID id, @RequestBody EmergencyPlanRequest r) {
    var p = principal(); plan(id);
    var e = emergencyPlans.save(new IndividualEmergencyPlan(UUID.fromString(p.facilityId()), p.governorate(), id, r.triggers(), r.actions(), r.contacts(), r.reviewedAt()));
    audit.record("complex.emergency_plan.set","individual_emergency_plan",e.getId().toString(),"{}");
    return Map.of("ok",true,"emergency_plan",e);
  }
  public record CloseRequest(BigDecimal qualityOfLifeScore) {}
  @PostMapping("/plans/{id}/close")
  Map<String,Object> close(@PathVariable UUID id, @RequestBody CloseRequest r) {
    principal(); var pl = plan(id);
    if (!ComplexCareRules.closurePermitted(r.qualityOfLifeScore() == null ? null : r.qualityOfLifeScore().doubleValue(), 0))
      throw BizException.badRequest("qol_required","إغلاق الخطة يقيس جودة حياة المريض — لا إغلاق صامت");
    pl.close(r.qualityOfLifeScore()); plans.save(pl);
    audit.record("complex.plan.closed","complex_care_plan",id.toString(),"{}");
    return Map.of("ok",true,"plan",pl);
  }
}
