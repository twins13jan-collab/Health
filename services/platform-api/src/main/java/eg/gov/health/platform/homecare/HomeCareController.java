package eg.gov.health.platform.homecare;
import eg.gov.health.platform.common.*;
import eg.gov.health.platform.criticalcare.IcuRules;
import org.springframework.web.bind.annotation.*;
import java.time.*; import java.util.*;
/** Home care & community nursing (cycle 253). Visits bind to a
 *  physician-approved plan, a team and real times; missed access creates
 *  a follow-up duty; deterioration at home escalates; devices are tracked
 *  by serial with delivery condition and maintenance; remote readings
 *  obey the freshness law — never shown live when disconnected. Only a
 *  coarse zone is stored, never the patient's exact home address. */
@RestController @RequestMapping("/api/home-care")
public class HomeCareController {
  private final HomeCareReferralRepository referrals; private final HomeCarePlanRepository plans;
  private final HomeVisitRepository visits; private final HomeDeviceRepository devices;
  private final RemoteObservationRepository observations; private final AuditService audit;
  public HomeCareController(HomeCareReferralRepository referrals, HomeCarePlanRepository plans,
                            HomeVisitRepository visits, HomeDeviceRepository devices,
                            RemoteObservationRepository observations, AuditService audit) {
    this.referrals=referrals; this.plans=plans; this.visits=visits; this.devices=devices;
    this.observations=observations; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private HomeCareReferral referral(UUID id) {
    return referrals.findById(id).orElseThrow(() -> BizException.notFound("referral_not_found","إحالة الرعاية المنزلية غير موجودة"));
  }
  private HomeCarePlan plan(UUID id) {
    return plans.findById(id).orElseThrow(() -> BizException.notFound("plan_not_found","خطة الرعاية المنزلية غير موجودة"));
  }
  private HomeVisit visit(UUID id) {
    return visits.findById(id).orElseThrow(() -> BizException.notFound("visit_not_found","الزيارة غير موجودة"));
  }

  public record ReferralRequest(UUID patientId, String reason, String need, String zone, String safetyRequirements) {}
  @PostMapping("/referrals")
  Map<String,Object> refer(@RequestBody ReferralRequest r) {
    var p = principal();
    if (r.patientId()==null||r.reason()==null) throw BizException.badRequest("invalid_referral","المريض وسبب الإحالة مطلوبان");
    var ref = referrals.save(new HomeCareReferral(UUID.fromString(p.facilityId()), p.governorate(), r.patientId(),
      r.reason(), r.need(), r.zone(), r.safetyRequirements()));
    audit.record("homecare.referral.received","home_care_referral",ref.getId().toString(),"{}");
    return Map.of("ok",true,"referral",ref,"location_granularity","zone_only — exact home address is never stored");
  }

  public record PlanRequest(String goals, String frequency, String instructions, String escalationSigns, String approvedBy) {}
  @PostMapping("/referrals/{id}/plan")
  Map<String,Object> plan(@PathVariable UUID id, @RequestBody PlanRequest r) {
    var p = principal(); var ref = referral(id);
    if (r.approvedBy()==null||r.approvedBy().isBlank())
      throw BizException.badRequest("physician_approval_required","خطة الرعاية المنزلية وحدودها يعتمدها الطبيب (دورة 253)");
    var pl = plans.save(new HomeCarePlan(ref.getFacilityId(), p.governorate(), id, r.goals(), r.frequency(),
      r.instructions(), r.escalationSigns(), r.approvedBy()));
    ref.setStatus("planned"); referrals.save(ref);
    audit.record("homecare.plan.approved","home_care_plan",pl.getId().toString(),"{}");
    return Map.of("ok",true,"plan",pl);
  }

  public record VisitRequest(OffsetDateTime scheduledAt, String team) {}
  @PostMapping("/plans/{id}/visits")
  Map<String,Object> schedule(@PathVariable UUID id, @RequestBody VisitRequest r) {
    var p = principal(); var pl = plan(id);
    if (!HomeCareRules.visitsAllowed(pl.getStatus()))
      throw BizException.conflict("plan_not_active","الزيارات تتطلب خطة نشطة معتمدة");
    if (r.scheduledAt()==null) throw BizException.badRequest("schedule_required","موعد الزيارة مطلوب");
    var v = visits.save(new HomeVisit(pl.getFacilityId(), p.governorate(), id, r.scheduledAt(), r.team()));
    audit.record("homecare.visit.assigned","home_visit",v.getId().toString(),"{}");
    return Map.of("ok",true,"visit",v);
  }
  @PostMapping("/visits/{id}/arrive")
  Map<String,Object> arrive(@PathVariable UUID id) {
    var v = visit(id);
    if (!"scheduled".equals(v.getStatus())) throw BizException.conflict("not_scheduled","الزيارة ليست مجدولة");
    v.arrive(); visits.save(v);
    audit.record("homecare.visit.started","home_visit",id.toString(),"{}");
    return Map.of("ok",true,"visit",v);
  }
  public record CompleteRequest(OffsetDateTime leftAt, String services, String outcome) {}
  @PostMapping("/visits/{id}/complete")
  Map<String,Object> complete(@PathVariable UUID id, @RequestBody CompleteRequest r) {
    var v = visit(id);
    if (!"in_progress".equals(v.getStatus())) throw BizException.conflict("not_started","لا إكمال دون وصول موثق أولًا");
    OffsetDateTime left = r.leftAt()==null ? OffsetDateTime.now() : r.leftAt();
    if (!HomeCareRules.visitTimesValid(v.getArrivedAt(), left))
      throw BizException.badRequest("invalid_times","وقت المغادرة يجب أن يلي الوصول");
    boolean esc = HomeCareRules.escalationRequired(r.outcome());
    v.complete(left, r.services(), r.outcome(), esc); visits.save(v);
    audit.record("homecare.visit.completed","home_visit",id.toString(),"{}");
    return Map.of("ok",true,"visit",v,"escalation_triggered",esc,
      "escalation_action", esc ? "deterioration at home — escalate to ambulance/facility now" : "");
  }
  @PostMapping("/visits/{id}/no-access")
  Map<String,Object> noAccess(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var v = visit(id);
    if (!"scheduled".equals(v.getStatus())) throw BizException.conflict("not_scheduled","الزيارة ليست مجدولة");
    String notes = body.get("notes");
    if (notes==null||notes.isBlank()) throw BizException.badRequest("attempt_notes_required","تعذر الوصول يتطلب توثيق المحاولة (دورة 253)");
    v.noAccess(notes); visits.save(v);
    audit.record("homecare.visit.no_access","home_visit",id.toString(),"{}");
    return Map.of("ok",true,"visit",v,"follow_up_required",true);
  }

  public record DeviceRequest(UUID patientId, String deviceType, String serialNumber, String conditionOnDelivery, LocalDate maintenanceDue) {}
  @PostMapping("/devices")
  Map<String,Object> deliverDevice(@RequestBody DeviceRequest r) {
    var p = principal();
    if (r.patientId()==null||r.deviceType()==null||r.serialNumber()==null||r.conditionOnDelivery()==null)
      throw BizException.badRequest("invalid_device","المريض والجهاز ورقمه التسلسلي وحالته عند التسليم مطلوبة");
    var fac = UUID.fromString(p.facilityId());
    boolean dup = !devices.findByFacilityIdAndSerialNumberAndStatusNot(fac, r.serialNumber(), "retrieved").isEmpty();
    if (dup) throw BizException.conflict("serial_in_use","الرقم التسلسلي مسلم أصلًا — لا تسليم مزدوج لجهاز منزلي");
    var d = devices.save(new HomeDevice(fac, p.governorate(), r.patientId(), r.deviceType(), r.serialNumber(), r.conditionOnDelivery(), r.maintenanceDue()));
    audit.record("homecare.device.delivered","home_device",d.getId().toString(),"{}");
    return Map.of("ok",true,"device",d);
  }
  @PostMapping("/devices/{id}/retrieve")
  Map<String,Object> retrieve(@PathVariable UUID id) {
    var d = devices.findById(id).orElseThrow(() -> BizException.notFound("device_not_found","الجهاز غير موجود"));
    if ("retrieved".equals(d.getStatus())) throw BizException.conflict("already_retrieved","الجهاز مسترجع مسبقًا");
    d.retrieve(); devices.save(d);
    audit.record("homecare.device.retrieved","home_device",id.toString(),"{}");
    return Map.of("ok",true,"device",d);
  }
  @GetMapping("/devices/{id}")
  Map<String,Object> device(@PathVariable UUID id) {
    var d = devices.findById(id).orElseThrow(() -> BizException.notFound("device_not_found","الجهاز غير موجود"));
    return Map.of("ok",true,"device",d,"maintenance_overdue", HomeCareRules.maintenanceOverdue(d.getMaintenanceDue(), LocalDate.now()));
  }

  public record ObservationRequest(UUID patientId, String deviceCode, String kind, Double value, String unit, OffsetDateTime sourceTime) {}
  @PostMapping("/observations")
  Map<String,Object> observe(@RequestBody ObservationRequest r) {
    var p = principal();
    if (r.patientId()==null||r.kind()==null||r.value()==null||r.sourceTime()==null)
      throw BizException.badRequest("invalid_observation","المريض والنوع والقيمة ووقت المصدر مطلوبة");
    var quality = IcuRules.qualityFor(r.kind(), r.value());
    var o = observations.save(new RemoteObservation(UUID.fromString(p.facilityId()), p.governorate(), r.patientId(),
      r.deviceCode(), r.kind(), r.value(), r.unit(), r.sourceTime(), quality));
    var f = HomeCareRules.freshness(r.sourceTime(), OffsetDateTime.now());
    audit.record("homecare.observation.received","remote_observation",o.getId().toString(),"{}");
    return Map.of("ok",true,"observation",o,"quality",quality,"freshness",f.name(),
      "present_as_live", HomeCareRules.mayPresentAsLive(f));
  }

  @GetMapping("/referrals")
  Map<String,Object> byPatient(@RequestParam UUID patientId) {
    var rows = referrals.findByPatientIdOrderByCreatedAtDesc(patientId);
    return Map.of("ok",true,"referrals",rows,"count",rows.size());
  }
}
