package eg.gov.health.platform.stroke;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.*; import java.util.*;
/** Stroke & acute neurology (module 88). Activation records
 *  last-known-well and arrival; neurology assessment (NIHSS) and CT
 *  are bound to decision clocks; thrombolysis follows the approved
 *  protocol: CT-excluded hemorrhage, a clean contraindication
 *  checklist, and the 4.5-hour window — with door-to-needle ≤ 60
 *  minutes flagged on breach. Urgent transfer needs documented
 *  acceptance from the capable centre. Monitoring covers bleeding,
 *  swallow, mobility and consciousness; no oral intake precedes a
 *  passed swallow screen; discharge binds rehab and secondary
 *  prevention. */
@RestController @RequestMapping("/api/stroke")
public class StrokeController {
  private final StrokeActivationRepository activations; private final ThrombolysisDecisionRepository decisions;
  private final StrokeTransferRepository transfers; private final StrokeMonitoringRepository monitoring;
  private final StrokeDischargePlanRepository discharge; private final AuditService audit;
  public StrokeController(StrokeActivationRepository activations, ThrombolysisDecisionRepository decisions,
                          StrokeTransferRepository transfers, StrokeMonitoringRepository monitoring,
                          StrokeDischargePlanRepository discharge, AuditService audit) {
    this.activations=activations; this.decisions=decisions; this.transfers=transfers;
    this.monitoring=monitoring; this.discharge=discharge; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private StrokeActivation activation(UUID id) {
    return activations.findById(id).orElseThrow(() -> BizException.notFound("activation_not_found","تفعيل السكتة غير موجود"));
  }

  public record ActivationRequest(String patientRef, OffsetDateTime lastKnownWellAt, OffsetDateTime arrivalAt, Integer nihss) {}
  @PostMapping("/activations")
  Map<String,Object> activate(@RequestBody ActivationRequest r) {
    var p = principal();
    if (r.lastKnownWellAt()==null||r.arrivalAt()==null)
      throw BizException.badRequest("clocks_required","مسار السكتة يسجل وقت آخر حالة طبيعية ووقت الوصول — القرار مبني على الساعات");
    var a = activations.save(new StrokeActivation(UUID.fromString(p.facilityId()), p.governorate(), r.patientRef(),
      r.lastKnownWellAt(), r.arrivalAt(), r.nihss()));
    audit.record("stroke.activated","stroke_activation",a.getId().toString(),"{}");
    return Map.of("ok",true,"activation",a,
      "onset_to_door_minutes",Duration.between(r.lastKnownWellAt(), r.arrivalAt()).toMinutes());
  }
  public record CtRequest(OffsetDateTime ctAt, String result, Boolean hemorrhageExcluded) {}
  @PostMapping("/activations/{id}/ct")
  Map<String,Object> ct(@PathVariable UUID id, @RequestBody CtRequest r) {
    var a = activation(id);
    a.recordCt(r.ctAt(), r.result(), Boolean.TRUE.equals(r.hemorrhageExcluded())); activations.save(a);
    audit.record("stroke.ct.recorded","stroke_activation",id.toString(),"{}");
    return Map.of("ok",true,"activation",a,
      "door_to_ct_minutes", r.ctAt()==null ? -1 : Duration.between(a.getArrivalAt(), r.ctAt()).toMinutes());
  }
  public record DecisionRequest(Boolean recentSurgery, Boolean activeBleeding, Boolean uncontrolledBp, Boolean anticoagulantUse,
                                Boolean lowGlucoseMimic, String decision, String reason, OffsetDateTime needleAt) {}
  @PostMapping("/activations/{id}/thrombolysis-decision")
  Map<String,Object> decide(@PathVariable UUID id, @RequestBody DecisionRequest r) {
    var p = principal(); var a = activation(id);
    boolean contra = StrokeRules.anyContraindication(Boolean.TRUE.equals(r.recentSurgery()), Boolean.TRUE.equals(r.activeBleeding()),
      Boolean.TRUE.equals(r.uncontrolledBp()), Boolean.TRUE.equals(r.anticoagulantUse()), Boolean.TRUE.equals(r.lowGlucoseMimic()));
    if ("given".equals(r.decision())) {
      if (r.needleAt()==null) throw BizException.badRequest("needle_time","وقت الإبرة يوثق قرار الإذابة");
      if (!StrokeRules.thrombolysisPermitted(a.isHemorrhageExcluded(), contra, a.getLastKnownWellAt(), r.needleAt())) {
        String why = !a.isHemorrhageExcluded() ? "hemorrhage not excluded by CT"
          : contra ? "an absolute contraindication is present"
          : "outside the 4.5-hour window from last known well";
        throw BizException.conflict("thrombolysis_blocked","منع الإذابة: " + why);
      }
    } else if (!"withheld".equals(r.decision()) || r.reason()==null) {
      throw BizException.badRequest("decision_fields","قرار المنع يوثق بسبب كما يوثق قرار الإعطاء");
    }
    var d = decisions.save(new ThrombolysisDecision(a.getFacilityId(), p.governorate(), id, Boolean.TRUE.equals(r.recentSurgery()),
      Boolean.TRUE.equals(r.activeBleeding()), Boolean.TRUE.equals(r.uncontrolledBp()), Boolean.TRUE.equals(r.anticoagulantUse()),
      Boolean.TRUE.equals(r.lowGlucoseMimic()), r.decision(), r.reason(), p.email(), r.needleAt()));
    a.setStatus("given".equals(r.decision()) ? "treated" : "decided"); activations.save(a);
    audit.record("stroke.thrombolysis.decided","thrombolysis_decision",d.getId().toString(),"{}");
    return Map.of("ok",true,"decision",d,
      "door_to_needle_minutes", r.needleAt()==null ? -1 : Duration.between(a.getArrivalAt(), r.needleAt()).toMinutes(),
      "door_to_needle_breached", StrokeRules.doorToNeedleBreached(a.getArrivalAt(), r.needleAt()));
  }
  public record TransferRequest(String destinationCenter, Boolean acceptanceDocumented, String acceptedBy, OffsetDateTime departedAt) {}
  @PostMapping("/activations/{id}/transfers")
  Map<String,Object> transfer(@PathVariable UUID id, @RequestBody TransferRequest r) {
    var p = principal(); var a = activation(id);
    // Urgent transfer only against documented acceptance by the capable centre.
    if (!Boolean.TRUE.equals(r.acceptanceDocumented()) || r.acceptedBy()==null)
      throw BizException.conflict("acceptance_required","النقل العاجل إلى مركز قادر يتطلب قبولًا موثقًا من المركز المستقبل");
    var t = transfers.save(new StrokeTransfer(a.getFacilityId(), p.governorate(), id, r.destinationCenter(), true, r.acceptedBy(), r.departedAt()));
    a.setStatus("transferred"); activations.save(a);
    audit.record("stroke.transferred","stroke_transfer",t.getId().toString(),"{}");
    return Map.of("ok",true,"transfer",t);
  }
  public record MonitoringRequest(String swallowScreen, Boolean oralIntake, String consciousness, String mobility, Boolean hemorrhageFlag, String notes) {}
  @PostMapping("/activations/{id}/monitoring")
  Map<String,Object> monitor(@PathVariable UUID id, @RequestBody MonitoringRequest r) {
    var p = principal(); var a = activation(id);
    if (Boolean.TRUE.equals(r.oralIntake()) && !StrokeRules.oralIntakePermitted(r.swallowScreen()))
      throw BizException.conflict("swallow_first","لا تناول بالفم قبل فحص بلع موثق ناجح");
    var m = monitoring.save(new StrokeMonitoring(a.getFacilityId(), p.governorate(), id, r.swallowScreen(),
      Boolean.TRUE.equals(r.oralIntake()), r.consciousness(), r.mobility(), Boolean.TRUE.equals(r.hemorrhageFlag()), r.notes()));
    audit.record("stroke.monitoring.recorded","stroke_monitoring",m.getId().toString(),"{}");
    return Map.of("ok",true,"monitoring",m);
  }
  public record DischargeRequest(String rehabReferral, String secondaryPrevention, String followUp) {}
  @PostMapping("/activations/{id}/discharge-plan")
  Map<String,Object> dischargePlan(@PathVariable UUID id, @RequestBody DischargeRequest r) {
    var p = principal(); var a = activation(id);
    if (r.rehabReferral()==null||r.secondaryPrevention()==null)
      throw BizException.badRequest("discharge_fields","الخروج يرتبط بالتأهيل والوقاية الثانوية");
    var d = discharge.save(new StrokeDischargePlan(a.getFacilityId(), p.governorate(), id, r.rehabReferral(), r.secondaryPrevention(), r.followUp()));
    audit.record("stroke.discharge.planned","stroke_discharge_plan",d.getId().toString(),"{}");
    return Map.of("ok",true,"discharge_plan",d);
  }
}
