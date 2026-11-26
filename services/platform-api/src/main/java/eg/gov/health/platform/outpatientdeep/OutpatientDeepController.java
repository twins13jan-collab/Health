package eg.gov.health.platform.outpatientdeep;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Outpatient clinics & specialist care (module 83). The visit
 *  cycle runs from request, identity and eligibility
 *  verification through triage, examination, the treatment plan
 *  and visit closure; clinics are scheduled by specialty,
 *  physician, room, equipment and real capacity; in-person,
 *  virtual, home and phone follow-up visits are managed;
 *  waiting lists, delays, cancellations, no-shows and
 *  rescheduling are governed; history, examination, diagnoses,
 *  orders, prescriptions and referrals are documented;
 *  multidisciplinary clinics name a follow-up owner; chronic,
 *  oncology, cardiology, immunology and pediatric clinics run;
 *  and waiting time, visit completeness and continuity of care
 *  are measured. */
@RestController @RequestMapping("/api/outpatient-deep")
public class OutpatientDeepController {
  private final ClinicVisitRepository visits; private final ClinicNoShowRepository noShows; private final AuditService audit;
  public OutpatientDeepController(ClinicVisitRepository visits, ClinicNoShowRepository noShows, AuditService audit) {
    this.visits=visits; this.noShows=noShows; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private ClinicVisit visit(UUID id) {
    return visits.findById(id).orElseThrow(() -> BizException.notFound("visit_not_found","الزيارة غير موجودة"));
  }

  public record VisitBody(String patientRef, String specialty, String visitType, Boolean identityVerified,
                          Boolean eligibilityVerified, Boolean planDocumented, String followupOwner) {}
  @PostMapping("/visits")
  Map<String,Object> create(@RequestBody VisitBody r) {
    var p = principal();
    var v = visits.save(new ClinicVisit(UUID.fromString(p.facilityId()), p.governorate(), r.patientRef(), r.specialty(), r.visitType(),
      Boolean.TRUE.equals(r.identityVerified()), Boolean.TRUE.equals(r.eligibilityVerified()), Boolean.TRUE.equals(r.planDocumented()), r.followupOwner()));
    return Map.of("ok",true,"visit",v);
  }
  @PostMapping("/visits/{id}/close")
  Map<String,Object> close(@PathVariable UUID id) {
    principal(); var v = visit(id);
    var blockers = OutpatientRules.closureBlockers(v.isIdentityVerified(), v.isEligibilityVerified(), v.isPlanDocumented(), v.getFollowupOwner());
    if (!blockers.isEmpty())
      throw BizException.conflict("visit_incomplete","زيارة لا تُغلق ناقصة: " + String.join("، ", blockers) + " — زيارة بلا مسؤول متابعة مسمى محادثة لا رعاية");
    v.close(); visits.save(v);
    return Map.of("ok",true,"visit",v);
  }
  public record NoShowBody(Boolean rescheduleOffered) {}
  @PostMapping("/visits/{id}/no-show")
  Map<String,Object> noShow(@PathVariable UUID id, @RequestBody NoShowBody r) {
    var p = principal(); var v = visit(id);
    v.markNoShow(); visits.save(v);
    var ns = noShows.save(new ClinicNoShow(UUID.fromString(p.facilityId()), p.governorate(), id, Boolean.TRUE.equals(r.rescheduleOffered())));
    audit.record("outpatient.no_show","clinic_no_show",ns.getId().toString(),"{}");
    return Map.of("ok",true,"no_show",ns,"reschedule_offered",Boolean.TRUE.equals(r.rescheduleOffered()),
      "note","a no-show is recorded and a reschedule offered — a patient silently dropped is lost by the system, not by choice");
  }
}
