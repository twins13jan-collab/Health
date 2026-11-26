package eg.gov.health.platform.clinical;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.OffsetDateTime; import java.util.*;
@RestController @RequestMapping("/api/appointments")
public class AppointmentController {
  private final AppointmentRepository repo; private final AuditService audit;
  public AppointmentController(AppointmentRepository repo, AuditService audit) { this.repo=repo; this.audit=audit; }
  public record ApptRequest(UUID patientId, String clinic, String doctorName, OffsetDateTime startsAt, OffsetDateTime endsAt) {}
  @PostMapping
  Map<String,Object> book(@RequestBody ApptRequest r) {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    if (r.patientId()==null||r.startsAt()==null||r.endsAt()==null||!r.endsAt().isAfter(r.startsAt()))
      throw BizException.badRequest("invalid_slot","فترة موعد غير صحيحة");
    var candidate = new Appointment(UUID.fromString(p.facilityId()), p.governorate(), r.patientId(), r.clinic(), r.doctorName(), r.startsAt(), r.endsAt());
    if (r.doctorName()!=null) {
      boolean clash = repo.findByFacilityIdAndDoctorName(candidate.getFacilityId(), r.doctorName()).stream().anyMatch(candidate::overlaps);
      if (clash) throw BizException.conflict("slot_conflict","تعارض مع موعد قائم لنفس الطبيب — لا حجز مزدوج");
    }
    var saved = repo.save(candidate);
    audit.record("appointment.booked","appointment",saved.getId().toString(),"{}");
    return Map.of("ok",true,"appointment",saved);
  }
  @PostMapping("/{id}/status")
  Map<String,Object> status(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var a = repo.findById(id).orElseThrow(() -> BizException.notFound("appointment_not_found","الموعد غير موجود"));
    String to = body.getOrDefault("status","");
    if (!List.of("booked","confirmed","attended","no_show","cancelled","rescheduled").contains(to))
      throw BizException.badRequest("invalid_status","حالة غير معروفة");
    a.setStatus(to); repo.save(a);
    audit.record("appointment."+to,"appointment",id.toString(),"{}");
    return Map.of("ok",true,"appointment",a);
  }
  @GetMapping
  Map<String,Object> byPatient(@RequestParam UUID patientId) {
    var rows = repo.findByPatientIdOrderByStartsAtAsc(patientId);
    return Map.of("ok",true,"appointments",rows,"count",rows.size());
  }
}
