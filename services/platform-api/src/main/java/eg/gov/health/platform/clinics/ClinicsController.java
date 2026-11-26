package eg.gov.health.platform.clinics;
import eg.gov.health.platform.clinical.*;
import eg.gov.health.platform.common.*;
import eg.gov.health.platform.patient.PatientRepository;
import org.springframework.web.bind.annotation.*;
import java.time.*; import java.util.*;
/** Outpatient clinics & scheduling (Phase 4.3): clinics, dated doctor
 *  sessions on a slot grid with capacity, booking into the next free
 *  slot, and a waiting list that auto-promotes (urgent first, then
 *  arrival) when a booking is cancelled with a documented reason. */
@RestController @RequestMapping("/api/clinics")
public class ClinicsController {
  private static final ZoneId CAIRO = ZoneId.of("Africa/Cairo");
  private final ClinicRepository clinics; private final ClinicSessionRepository sessions;
  private final WaitlistRepository waitlist; private final AppointmentRepository appointments;
  private final PatientRepository patients; private final AuditService audit;
  public ClinicsController(ClinicRepository clinics, ClinicSessionRepository sessions, WaitlistRepository waitlist,
                           AppointmentRepository appointments, PatientRepository patients, AuditService audit) {
    this.clinics = clinics; this.sessions = sessions; this.waitlist = waitlist;
    this.appointments = appointments; this.patients = patients; this.audit = audit;
  }
  private UUID facilityId() {
    var p = TenantContext.get();
    if (p == null || p.facilityId() == null) throw BizException.forbidden("يتطلب حساب منشأة");
    return UUID.fromString(p.facilityId());
  }
  private String governorate() { var p = TenantContext.get(); return p == null ? null : p.governorate(); }
  private Clinic clinicOwned(UUID id) {
    var c = clinics.findById(id).orElseThrow(() -> BizException.notFound("clinic_not_found", "العيادة غير موجودة"));
    if (!c.getFacilityId().equals(facilityId())) throw BizException.forbidden("العيادة لمنشأة أخرى");
    return c;
  }
  private ClinicSession sessionOwned(UUID id) {
    var s = sessions.findById(id).orElseThrow(() -> BizException.notFound("session_not_found", "الجلسة غير موجودة"));
    if (!s.getFacilityId().equals(facilityId())) throw BizException.forbidden("الجلسة لمنشأة أخرى");
    return s;
  }
  private String patientName(UUID id) {
    return patients.findById(id).map(p -> p.getFullName()).orElse("—");
  }
  private long bookedCount(UUID sessionId) { return appointments.countBySessionIdAndStatusNot(sessionId, "cancelled"); }

  public record ClinicRequest(String nameAr, String nameEn, String specialty, String room) {}
  public record SessionRequest(UUID clinicId, String doctorName, LocalDate sessionDate,
                               LocalTime startsAt, LocalTime endsAt, Integer slotMinutes, Integer capacity) {}
  public record BookRequest(UUID patientId) {}
  public record WaitlistRequest(UUID patientId, String reason, String priority) {}
  public record ReasonRequest(String reason) {}
  public record StatusRequest(String status) {}

  @GetMapping
  Map<String, Object> listClinics() {
    var rows = clinics.findByFacilityIdOrderByNameArAsc(facilityId());
    return Map.of("ok", true, "clinics", rows, "count", rows.size());
  }
  @PostMapping
  Map<String, Object> createClinic(@RequestBody ClinicRequest r) {
    if (r.nameAr() == null || r.nameAr().isBlank()) throw BizException.badRequest("name_required", "اسم العيادة مطلوب");
    var c = clinics.save(new Clinic(facilityId(), governorate(), r.nameAr(), r.nameEn(), r.specialty(), r.room()));
    audit.record("clinic.created", "clinic", c.getId().toString(), "{}");
    return Map.of("ok", true, "clinic", c);
  }
  @PostMapping("/{id}/deactivate")
  Map<String, Object> deactivate(@PathVariable UUID id) {
    var c = clinicOwned(id); c.setActive(false); clinics.save(c);
    audit.record("clinic.deactivated", "clinic", id.toString(), "{}");
    return Map.of("ok", true, "clinic", c);
  }

  @GetMapping("/sessions")
  Map<String, Object> listSessions(@RequestParam(required = false) LocalDate date) {
    var d = date == null ? LocalDate.now(CAIRO) : date;
    var rows = new ArrayList<Map<String, Object>>();
    for (var s : sessions.findByFacilityIdAndSessionDateOrderByStartsAtAsc(facilityId(), d)) {
      var m = new LinkedHashMap<String, Object>();
      m.put("session", s);
      m.put("clinicNameAr", clinics.findById(s.getClinicId()).map(Clinic::getNameAr).orElse("—"));
      m.put("booked", bookedCount(s.getId()));
      m.put("waiting", waitlist.countBySessionIdAndStatus(s.getId(), "waiting"));
      m.put("slots", ClinicRules.slotCount(s.getStartsAt(), s.getEndsAt(), s.getSlotMinutes()));
      rows.add(m);
    }
    return Map.of("ok", true, "date", d, "sessions", rows, "count", rows.size());
  }
  @PostMapping("/sessions")
  Map<String, Object> createSession(@RequestBody SessionRequest r) {
    var clinic = clinicOwned(r.clinicId());
    if (!clinic.isActive()) throw BizException.badRequest("clinic_inactive", "العيادة موقوفة");
    int slot = r.slotMinutes() == null ? 15 : r.slotMinutes();
    int cap = r.capacity() == null ? 0 : r.capacity();
    var problem = ClinicRules.sessionProblem(r.startsAt(), r.endsAt(), slot, cap);
    if (problem != null) throw BizException.badRequest("invalid_session", problem);
    if (r.doctorName() == null || r.doctorName().isBlank()) throw BizException.badRequest("doctor_required", "اسم الطبيب مطلوب");
    boolean clash = sessions.findByFacilityIdAndSessionDateOrderByStartsAtAsc(facilityId(), r.sessionDate()).stream()
      .filter(x -> x.getDoctorName().equals(r.doctorName()) && !"cancelled".equals(x.getStatus()))
      .anyMatch(x -> r.startsAt().isBefore(x.getEndsAt()) && x.getStartsAt().isBefore(r.endsAt()));
    if (clash) throw BizException.conflict("doctor_clash", "الطبيب مرتبط بجلسة متداخلة في نفس اليوم");
    var s = sessions.save(new ClinicSession(clinic.getId(), facilityId(), governorate(), r.doctorName(),
      r.sessionDate(), r.startsAt(), r.endsAt(), slot, cap));
    audit.record("clinic.session_created", "clinic_session", s.getId().toString(), "{}");
    return Map.of("ok", true, "session", s);
  }
  @PostMapping("/sessions/{id}/status")
  Map<String, Object> sessionStatus(@PathVariable UUID id, @RequestBody StatusRequest r) {
    var s = sessionOwned(id);
    if (!List.of("open", "closed", "cancelled", "scheduled").contains(r.status()))
      throw BizException.badRequest("invalid_status", "حالة جلسة غير معروفة");
    s.setStatus(r.status()); sessions.save(s);
    audit.record("clinic.session_" + r.status(), "clinic_session", id.toString(), "{}");
    return Map.of("ok", true, "session", s);
  }

  @GetMapping("/sessions/{id}/board")
  Map<String, Object> board(@PathVariable UUID id) {
    var s = sessionOwned(id);
    var queue = new ArrayList<Map<String, Object>>();
    for (var a : appointments.findBySessionIdOrderByStartsAtAsc(id)) {
      var m = new LinkedHashMap<String, Object>();
      m.put("appointment", a); m.put("patientName", patientName(a.getPatientId()));
      queue.add(m);
    }
    var waiting = waitlist.findBySessionIdAndStatusOrderByCreatedAtAsc(id, "waiting");
    waiting.sort(ClinicRules.promotionOrder());
    var wrows = new ArrayList<Map<String, Object>>();
    for (var w : waiting) {
      var m = new LinkedHashMap<String, Object>();
      m.put("entry", w); m.put("patientName", patientName(w.getPatientId()));
      wrows.add(m);
    }
    var m = new LinkedHashMap<String, Object>();
    m.put("ok", true); m.put("session", s);
    m.put("clinicNameAr", clinics.findById(s.getClinicId()).map(Clinic::getNameAr).orElse("—"));
    m.put("booked", bookedCount(id)); m.put("queue", queue); m.put("waitlist", wrows);
    return m;
  }

  @PostMapping("/sessions/{id}/book")
  Map<String, Object> book(@PathVariable UUID id, @RequestBody BookRequest r) {
    var s = sessionOwned(id);
    if (r.patientId() == null || patients.findById(r.patientId()).isEmpty())
      throw BizException.badRequest("patient_required", "المريض غير موجود في السجل الموحد");
    boolean already = appointments.existsBySessionIdAndPatientIdAndStatusNot(id, r.patientId(), "cancelled");
    var problem = ClinicRules.bookingProblem(s.getStatus(), s.getSessionDate(), LocalDate.now(CAIRO),
      bookedCount(id), s.getCapacity(), already);
    if (problem != null) throw BizException.conflict("booking_refused", problem);
    int idx = (int) bookedCount(id);
    var starts = s.slotStart(idx, CAIRO);
    var ends = starts.plusMinutes(s.getSlotMinutes());
    var clinic = clinics.findById(s.getClinicId()).orElseThrow();
    var a = new Appointment(facilityId(), governorate(), r.patientId(), clinic.getNameAr(), s.getDoctorName(), starts, ends);
    a.setSessionId(id);
    var saved = appointments.save(a);
    audit.record("clinic.booked", "appointment", saved.getId().toString(), "{}");
    return Map.of("ok", true, "appointment", saved, "slot", idx + 1);
  }

  @PostMapping("/sessions/{id}/waitlist")
  Map<String, Object> joinWaitlist(@PathVariable UUID id, @RequestBody WaitlistRequest r) {
    var s = sessionOwned(id);
    if ("cancelled".equals(s.getStatus()) || "closed".equals(s.getStatus()))
      throw BizException.conflict("session_closed", "الجلسة غير متاحة لقائمة الانتظار");
    if (r.patientId() == null || patients.findById(r.patientId()).isEmpty())
      throw BizException.badRequest("patient_required", "المريض غير موجود في السجل الموحد");
    var w = waitlist.save(new ClinicWaitlistEntry(id, facilityId(), governorate(), r.patientId(), r.reason(), r.priority()));
    audit.record("clinic.waitlisted", "waitlist", w.getId().toString(), "{}");
    return Map.of("ok", true, "entry", w);
  }

  /** Cancel a session booking (reason required) and auto-promote the
   *  first waiting patient — urgent first, then arrival — into the
   *  freed slot. */
  @PostMapping("/appointments/{id}/cancel")
  Map<String, Object> cancelAndPromote(@PathVariable UUID id, @RequestBody ReasonRequest r) {
    var problem = ClinicRules.cancelProblem(r.reason());
    if (problem != null) throw BizException.badRequest("reason_required", problem);
    var a = appointments.findById(id).orElseThrow(() -> BizException.notFound("appointment_not_found", "الموعد غير موجود"));
    if (!a.getFacilityId().equals(facilityId())) throw BizException.forbidden("الموعد لمنشأة أخرى");
    if ("cancelled".equals(a.getStatus())) throw BizException.conflict("already_cancelled", "الموعد ملغي أصلًا");
    a.setStatus("cancelled"); a.setCancelReason(r.reason()); appointments.save(a);
    audit.record("clinic.booking_cancelled", "appointment", id.toString(), "{}");
    Map<String, Object> promoted = null;
    if (a.getSessionId() != null) {
      var waiting = waitlist.findBySessionIdAndStatusOrderByCreatedAtAsc(a.getSessionId(), "waiting");
      waiting.sort(ClinicRules.promotionOrder());
      if (!waiting.isEmpty()) {
        var w = waiting.get(0);
        var s = sessions.findById(a.getSessionId()).orElseThrow();
        var clinic = clinics.findById(s.getClinicId()).orElseThrow();
        var na = new Appointment(facilityId(), governorate(), w.getPatientId(), clinic.getNameAr(),
          s.getDoctorName(), a.getStartsAt(), a.getEndsAt());
        na.setSessionId(a.getSessionId());
        var savedNew = appointments.save(na);
        w.setStatus("promoted"); waitlist.save(w);
        audit.record("clinic.waitlist_promoted", "appointment", savedNew.getId().toString(), "{}");
        promoted = Map.of("appointment", savedNew, "patientName", patientName(w.getPatientId()));
      }
    }
    var m = new LinkedHashMap<String, Object>();
    m.put("ok", true); m.put("cancelled", a); m.put("promoted", promoted);
    return m;
  }

  @PostMapping("/waitlist/{id}/cancel")
  Map<String, Object> cancelWaitlist(@PathVariable UUID id) {
    var w = waitlist.findById(id).orElseThrow(() -> BizException.notFound("waitlist_not_found", "قيد الانتظار غير موجود"));
    if (!w.getFacilityId().equals(facilityId())) throw BizException.forbidden("القيد لمنشأة أخرى");
    w.setStatus("cancelled"); waitlist.save(w);
    audit.record("clinic.waitlist_cancelled", "waitlist", id.toString(), "{}");
    return Map.of("ok", true, "entry", w);
  }
}
