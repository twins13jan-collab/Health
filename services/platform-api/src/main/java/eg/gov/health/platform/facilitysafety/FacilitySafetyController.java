package eg.gov.health.platform.facilitysafety;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.*; import java.math.*; import java.util.*;
/** Facility security, safety & civil protection (module 107). Fire
 *  and evacuation plans name routes and assembly points; alarms,
 *  extinguishers, medical gases, generators and exit lights are
 *  inspected on schedule and a failure carries defects and a
 *  corrective action. Hot-work permits are approved independently —
 *  never by the requester — with a fire watch for welding and
 *  lockout/tagout for electrical isolation, inside a validity
 *  window. Security incidents (assault, theft, asset loss,
 *  intrusion) are recorded with their response. Sensitive-area
 *  access is approved and time-boxed, escorted for visitors and
 *  contractors. Evacuation drills measure real evacuation time. */
@RestController @RequestMapping("/api/facility-safety")
public class FacilitySafetyController {
  private final FireSafetyPlanRepository plans; private final SafetyInspectionRepository inspections;
  private final HotWorkPermitRepository permits; private final FacilitySecurityIncidentRepository incidents;
  private final SensitiveAccessGrantRepository grants; private final EvacuationDrillRepository drills;
  private final AuditService audit;
  public FacilitySafetyController(FireSafetyPlanRepository plans, SafetyInspectionRepository inspections, HotWorkPermitRepository permits,
                                  FacilitySecurityIncidentRepository incidents, SensitiveAccessGrantRepository grants,
                                  EvacuationDrillRepository drills, AuditService audit) {
    this.plans=plans; this.inspections=inspections; this.permits=permits; this.incidents=incidents;
    this.grants=grants; this.drills=drills; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }

  public record PlanRequest(String building, String evacuationRoutes, String assemblyPoints, LocalDate reviewDue) {}
  @PostMapping("/fire-plans")
  Map<String,Object> firePlan(@RequestBody PlanRequest r) {
    var p = principal();
    if (r.assemblyPoints()==null||r.evacuationRoutes()==null)
      throw BizException.badRequest("plan_fields","خطة الحريق والإخلاء تسمي مسارات الإخلاء ونقاط التجمع");
    var pl = plans.save(new FireSafetyPlan(UUID.fromString(p.facilityId()), p.governorate(), r.building(), r.evacuationRoutes(), r.assemblyPoints(), p.email(), r.reviewDue()));
    audit.record("safety.fireplan.created","fire_safety_plan",pl.getId().toString(),"{}");
    return Map.of("ok",true,"plan",pl);
  }
  public record InspectionRequest(String systemType, String location, String result, String defects, LocalDate nextDue, String correctiveAction) {}
  @PostMapping("/inspections")
  Map<String,Object> inspect(@RequestBody InspectionRequest r) {
    var p = principal();
    // A failed inspection without defects and a corrective action is not an inspection record.
    if ("fail".equals(r.result()) && (r.defects()==null || r.correctiveAction()==null))
      throw BizException.badRequest("failure_fields","فحص فاشل يوثق العيوب والإجراء التصحيحي");
    var i = inspections.save(new SafetyInspection(UUID.fromString(p.facilityId()), p.governorate(), r.systemType(), r.location(), r.result(), r.defects(), p.email(), r.nextDue(), r.correctiveAction()));
    audit.record("safety.inspection.recorded","safety_inspection",i.getId().toString(),"{}");
    return Map.of("ok",true,"inspection",i);
  }
  public record PermitRequest(String workType, String location, Boolean fireWatch, Boolean lotoApplied, OffsetDateTime validFrom, OffsetDateTime validUntil) {}
  @PostMapping("/permits")
  Map<String,Object> permit(@RequestBody PermitRequest r) {
    var p = principal();
    var pm = permits.save(new HotWorkPermit(UUID.fromString(p.facilityId()), p.governorate(), r.workType(), r.location(), p.email(),
      Boolean.TRUE.equals(r.fireWatch()), Boolean.TRUE.equals(r.lotoApplied()), r.validFrom(), r.validUntil()));
    audit.record("safety.permit.requested","hot_work_permit",pm.getId().toString(),"{}");
    return Map.of("ok",true,"permit",pm);
  }
  @PostMapping("/permits/{id}/approve")
  Map<String,Object> approvePermit(@PathVariable UUID id) {
    var p = principal();
    var pm = permits.findById(id).orElseThrow(() -> BizException.notFound("permit_not_found","التصريح غير موجود"));
    if (!SafetyRules.permitApprovable(pm.getRequestedBy(), p.email(), pm.getWorkType(), pm.isFireWatch(), pm.isLotoApplied()))
      throw BizException.conflict("not_approvable","التصريح يعتمد من غير مقدمه — واللحام يحتاج مراقبة حريق والعزل الكهربائي يحتاج قفلًا ووسمًا");
    pm.approve(p.email()); permits.save(pm);
    audit.record("safety.permit.approved","hot_work_permit",id.toString(),"{}");
    return Map.of("ok",true,"permit",pm);
  }
  @PostMapping("/permits/{id}/start-work")
  Map<String,Object> startWork(@PathVariable UUID id) {
    var pm = permits.findById(id).orElseThrow(() -> BizException.notFound("permit_not_found","التصريح غير موجود"));
    if (!"approved".equals(pm.getStatus()) || !SafetyRules.withinValidity(pm.getValidFrom(), pm.getValidUntil(), OffsetDateTime.now()))
      throw BizException.conflict("outside_validity","العمل يبدأ بتصريح معتمد وداخل نافذة صلاحيته فقط");
    pm.activate(); permits.save(pm);
    audit.record("safety.permit.activated","hot_work_permit",id.toString(),"{}");
    return Map.of("ok",true,"permit",pm);
  }
  public record IncidentRequest(String kind, String location, String severity, String actionTaken) {}
  @PostMapping("/incidents")
  Map<String,Object> incident(@RequestBody IncidentRequest r) {
    var p = principal();
    var i = incidents.save(new FacilitySecurityIncident(UUID.fromString(p.facilityId()), p.governorate(), r.kind(), r.location(), r.severity(), p.email(), r.actionTaken()));
    audit.record("safety.incident.reported","facility_security_incident",i.getId().toString(),"{}");
    return Map.of("ok",true,"incident",i);
  }
  public record GrantRequest(String person, String personType, String sensitiveArea, String escortedBy, OffsetDateTime validFrom, OffsetDateTime validUntil) {}
  @PostMapping("/access-grants")
  Map<String,Object> grant(@RequestBody GrantRequest r) {
    var p = principal();
    var g = grants.save(new SensitiveAccessGrant(UUID.fromString(p.facilityId()), p.governorate(), r.person(), r.personType(), r.sensitiveArea(), r.escortedBy(), p.email(), r.validFrom(), r.validUntil()));
    audit.record("safety.access.granted","sensitive_access_grant",g.getId().toString(),"{}");
    return Map.of("ok",true,"grant",g,"access_now_allowed",
      SafetyRules.accessAllowed(g.getPersonType(), g.getApprovedBy()!=null, g.getEscortedBy(), g.getValidFrom(), g.getValidUntil(), OffsetDateTime.now()));
  }
  public record DrillRequest(String building, LocalDate drillDate, Integer participants, BigDecimal evacuationMinutes, String deficiencies) {}
  @PostMapping("/drills")
  Map<String,Object> drill(@RequestBody DrillRequest r) {
    var p = principal();
    var d = drills.save(new EvacuationDrill(UUID.fromString(p.facilityId()), p.governorate(), r.building(), r.drillDate(), r.participants(), r.evacuationMinutes(), r.deficiencies(), p.email()));
    audit.record("safety.drill.conducted","evacuation_drill",d.getId().toString(),"{}");
    return Map.of("ok",true,"drill",d,"result",d.getResult(),"target_minutes",8);
  }
}
