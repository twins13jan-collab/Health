package eg.gov.health.platform.emergencytriage;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.*; import java.util.*;
/** Emergency, triage & resuscitation (module 100). Arrival,
 *  triage, acuity level and re-evaluation are managed;
 *  resuscitation, trauma, burns, poisoning, stroke and MI
 *  pathways run; emergency orders, medications, procedures and
 *  consultations are coordinated; handovers between teams and
 *  departments are documented; door-to-doctor and intervention
 *  times are measured; refusal of service and leaving before
 *  assessment completes are governed; and crowding, diversions
 *  and capacity are watched. */
@RestController @RequestMapping("/api/emergency-triage")
public class EmergencyTriageController {
  private final TriageAssessmentRepository assessments; private final ErDepartureRepository departures; private final AuditService audit;
  public EmergencyTriageController(TriageAssessmentRepository assessments, ErDepartureRepository departures, AuditService audit) {
    this.assessments=assessments; this.departures=departures; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private TriageAssessment assessment(UUID id) {
    return assessments.findById(id).orElseThrow(() -> BizException.notFound("assessment_not_found","التقييم غير موجود"));
  }

  public record TriageBody(String patientRef, Integer esiLevel, OffsetDateTime arrivedAt) {}
  @PostMapping("/assessments")
  Map<String,Object> create(@RequestBody TriageBody r) {
    var p = principal();
    var a = assessments.save(new TriageAssessment(UUID.fromString(p.facilityId()), p.governorate(), r.patientRef(), r.esiLevel(),
      r.arrivedAt() != null ? r.arrivedAt() : OffsetDateTime.now()));
    return Map.of("ok",true,"assessment",a,"door_to_doctor_target_minutes",TriageRules.targetMinutes(r.esiLevel()));
  }
  public record SeenBody(OffsetDateTime seenAt) {}
  @PostMapping("/assessments/{id}/doctor-seen")
  Map<String,Object> seen(@PathVariable UUID id, @RequestBody SeenBody r) {
    principal(); var a = assessment(id);
    var seenAt = r.seenAt() != null ? r.seenAt() : OffsetDateTime.now();
    boolean breached = TriageRules.doorToDoctorBreached(a.getEsiLevel(), a.getArrivedAt(), seenAt);
    a.doctorSeen(seenAt); assessments.save(a);
    return Map.of("ok",true,"assessment",a,"door_to_doctor_breached",breached,
      "minutes",Duration.between(a.getArrivedAt(), seenAt).toMinutes());
  }
  public record DepartureBody(String departureType, Boolean risksExplained, Boolean signed) {}
  @PostMapping("/assessments/{id}/departures")
  Map<String,Object> departure(@PathVariable UUID id, @RequestBody DepartureBody r) {
    var p = principal(); var a = assessment(id);
    boolean documented = TriageRules.departureDocumented(Boolean.TRUE.equals(r.risksExplained()), Boolean.TRUE.equals(r.signed()));
    var d = departures.save(new ErDeparture(UUID.fromString(p.facilityId()), p.governorate(), id, r.departureType(),
      Boolean.TRUE.equals(r.risksExplained()), Boolean.TRUE.equals(r.signed())));
    if (documented) { a.close(); assessments.save(a); }
    else audit.record("triage.unsafe_departure","er_departure",d.getId().toString(),"{}");
    return Map.of("ok",true,"departure",d,"documented",documented,"encounter_closed",documented,
      "note","autonomy is real — but an undocumented walk-out is an unsafe departure, never a clean closure");
  }
}
