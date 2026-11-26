package eg.gov.health.platform.portal;
import eg.gov.health.platform.clinical.*;
import eg.gov.health.platform.clinics.*;
import eg.gov.health.platform.common.*;
import eg.gov.health.platform.facility.*;
import eg.gov.health.platform.identity.*;
import eg.gov.health.platform.patient.*;
import org.springframework.web.bind.annotation.*;
import java.time.*;
import java.util.*;

/** The patient & citizen portal (restructure wave): a registered
 *  patient sees their own record summary, browses bookable clinic
 *  sessions by facility, books and cancels their own appointments,
 *  and sends service requests (lab / pharmacy / home visit) to a
 *  facility inbox. Staff of a facility work that inbox here too.
 *  A patient can only ever touch their own data: the patient id
 *  comes from their account link, never from the request. */
@RestController @RequestMapping("/api/portal")
public class PortalController {
  private static final ZoneId CAIRO = ZoneId.of("Africa/Cairo");
  private final AppUserRepository users;
  private final PatientRepository patients;
  private final FacilityRepository facilities;
  private final ClinicRepository clinics;
  private final ClinicSessionRepository sessions;
  private final AppointmentRepository appointments;
  private final PortalServiceRequestRepository requests;
  private final AuditService audit;

  public PortalController(AppUserRepository users, PatientRepository patients, FacilityRepository facilities,
                          ClinicRepository clinics, ClinicSessionRepository sessions,
                          AppointmentRepository appointments, PortalServiceRequestRepository requests,
                          AuditService audit) {
    this.users = users; this.patients = patients; this.facilities = facilities;
    this.clinics = clinics; this.sessions = sessions; this.appointments = appointments;
    this.requests = requests; this.audit = audit;
  }

  private AppUser me() {
    var p = TenantContext.get();
    if (p == null) throw BizException.forbidden("يتطلب تسجيل الدخول");
    return users.findByEmail(p.email()).orElseThrow(() -> BizException.forbidden("الحساب غير موجود"));
  }
  private Patient myPatient() {
    var u = me();
    if (u.getPatientId() == null) throw BizException.forbidden("هذا الحساب غير مرتبط بسجل مريض");
    return patients.findById(u.getPatientId()).orElseThrow(() -> BizException.notFound("patient_not_found", "سجل المريض غير موجود"));
  }

  @GetMapping("/me")
  Map<String, Object> meInfo() {
    var u = me();
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("user", UserViews.of(u));
    if (u.getPatientId() != null) {
      patients.findById(u.getPatientId()).ifPresent(pt -> out.put("patient", pt));
      var mine = appointments.findByPatientIdOrderByStartsAtAsc(u.getPatientId());
      out.put("upcomingAppointments", mine.stream()
          .filter(a -> "booked".equals(a.getStatus()) && !a.getStartsAt().isBefore(OffsetDateTime.now()))
          .limit(5).toList());
      out.put("totalAppointments", mine.size());
    }
    return out;
  }

  // ---------------------------------------------------------- booking
  @GetMapping("/facilities/{facilityId}/sessions")
  Map<String, Object> facilitySessions(@PathVariable UUID facilityId,
                                       @RequestParam(required = false) LocalDate date) {
    LocalDate d = date == null ? LocalDate.now(CAIRO) : date;
    var clinicNames = new HashMap<UUID, String>();
    clinics.findByFacilityIdOrderByNameArAsc(facilityId).forEach(c -> clinicNames.put(c.getId(), c.getNameAr()));
    var views = sessions.findByFacilityIdAndSessionDateOrderByStartsAtAsc(facilityId, d).stream()
        .filter(s -> "open".equals(s.getStatus()))
        .map(s -> {
          long booked = appointments.findBySessionIdOrderByStartsAtAsc(s.getId()).stream()
              .filter(a -> !"cancelled".equals(a.getStatus())).count();
          Map<String, Object> v = new LinkedHashMap<>();
          v.put("session", s);
          v.put("clinicName", clinicNames.getOrDefault(s.getClinicId(), ""));
          v.put("booked", booked);
          v.put("remaining", Math.max(0, s.getCapacity() - booked));
          return v;
        }).toList();
    return Map.of("date", String.valueOf(d), "sessions", views);
  }

  public record BookRequest(UUID sessionId) {}
  @PostMapping("/book")
  Map<String, Object> book(@RequestBody BookRequest r) {
    var patient = myPatient();
    var session = sessions.findById(r.sessionId())
        .orElseThrow(() -> BizException.notFound("session_not_found", "الجلسة غير موجودة"));
    var existing = appointments.findBySessionIdOrderByStartsAtAsc(session.getId());
    long bookedCount = existing.stream().filter(a -> !"cancelled".equals(a.getStatus())).count();
    boolean already = existing.stream().anyMatch(a -> patient.getId().equals(a.getPatientId())
        && !"cancelled".equals(a.getStatus()));
    String problem = ClinicRules.bookingProblem(session.getStatus(), session.getSessionDate(),
        LocalDate.now(CAIRO), bookedCount, session.getCapacity(), already);
    if (problem != null) throw BizException.conflict("booking_problem", problem);
    int slot = (int) existing.stream().filter(a -> !"cancelled".equals(a.getStatus())).count();
    var clinic = clinics.findById(session.getClinicId()).orElse(null);
    var starts = session.slotStart(slot, CAIRO);
    var appt = new Appointment(session.getFacilityId(), session.getGovernorate(), patient.getId(),
        clinic == null ? null : clinic.getNameAr(), session.getDoctorName(),
        starts, starts.plusMinutes(session.getSlotMinutes()));
    appt.setSessionId(session.getId());
    appointments.save(appt);
    audit.record("portal.appointment_booked", "appointment", String.valueOf(appt.getId()), "{}");
    return Map.of("appointment", appt);
  }

  @GetMapping("/my-appointments")
  Map<String, Object> myAppointments() {
    var patient = myPatient();
    return Map.of("appointments", appointments.findByPatientIdOrderByStartsAtAsc(patient.getId()));
  }

  public record CancelRequest(String reason) {}
  @PostMapping("/appointments/{id}/cancel")
  Map<String, Object> cancelMine(@PathVariable UUID id, @RequestBody CancelRequest r) {
    var patient = myPatient();
    var a = appointments.findById(id).orElseThrow(() -> BizException.notFound("appointment_not_found", "الموعد غير موجود"));
    if (!patient.getId().equals(a.getPatientId())) throw BizException.forbidden("هذا الموعد ليس لك");
    String problem = ClinicRules.cancelProblem(r.reason());
    if (problem != null) throw BizException.badRequest("cancel_rule", problem);
    if (!"booked".equals(a.getStatus())) throw BizException.conflict("not_booked", "الموعد ليس محجوزًا");
    a.setStatus("cancelled");
    a.setCancelReason(r.reason());
    appointments.save(a);
    audit.record("portal.appointment_cancelled", "appointment", String.valueOf(id), "{}");
    return Map.of("appointment", a);
  }

  // ---------------------------------------------------------- service requests
  public record ServiceRequestBody(UUID facilityId, String serviceType, String details, LocalDate preferredDate) {}
  @PostMapping("/service-requests")
  Map<String, Object> createRequest(@RequestBody ServiceRequestBody r) {
    var patient = myPatient();
    if (r.facilityId() == null || r.serviceType() == null)
      throw BizException.badRequest("request_fields", "المنشأة ونوع الخدمة مطلوبان");
    if (!Set.of("lab", "pharmacy", "radiology", "dialysis", "home_visit", "other").contains(r.serviceType()))
      throw BizException.badRequest("service_type_invalid", "نوع خدمة غير معروف");
    var facility = facilities.findById(r.facilityId())
        .orElseThrow(() -> BizException.notFound("facility_not_found", "المنشأة غير موجودة"));
    if (!"active".equals(facility.getStatus())) throw BizException.conflict("facility_not_active", "المنشأة غير مفعّلة");
    var req = requests.save(new PortalServiceRequest(facility.getId(), facility.getGovernorate(),
        patient.getId(), me().getEmail(), r.serviceType(), r.details(), r.preferredDate()));
    audit.record("portal.service_requested", "portal_service_request", String.valueOf(req.getId()),
        "{\"type\":\"" + r.serviceType() + "\"}");
    return Map.of("request", req);
  }

  @GetMapping("/my-requests")
  Map<String, Object> myRequests() {
    return Map.of("requests", requests.findByPatientIdOrderByCreatedAtDesc(myPatient().getId()));
  }

  // ---------------------------------------------------------- staff inbox
  private UUID staffFacility() {
    var p = TenantContext.get();
    if (p == null || p.facilityId() == null) throw BizException.forbidden("يتطلب حساب منشأة");
    if ("patient".equals(p.role())) throw BizException.forbidden("صندوق الطلبات لطاقم المنشأة");
    return UUID.fromString(p.facilityId());
  }

  @GetMapping("/inbox")
  Map<String, Object> inbox(@RequestParam(required = false) String status) {
    UUID f = staffFacility();
    var rows = status == null
        ? requests.findByFacilityIdOrderByCreatedAtDesc(f)
        : requests.findByFacilityIdAndStatusOrderByCreatedAtAsc(f, status);
    var views = rows.stream().map(req -> {
      Map<String, Object> v = new LinkedHashMap<>();
      v.put("request", req);
      v.put("patientName", patients.findById(req.getPatientId()).map(pt -> pt.getFullName()).orElse("—"));
      return v;
    }).toList();
    return Map.of("requests", views, "count", views.size());
  }

  public record StatusRequest(String status, String note) {}
  @PostMapping("/inbox/{id}/status")
  Map<String, Object> setRequestStatus(@PathVariable UUID id, @RequestBody StatusRequest r) {
    UUID f = staffFacility();
    var req = requests.findById(id).orElseThrow(() -> BizException.notFound("request_not_found", "الطلب غير موجود"));
    if (!f.equals(req.getFacilityId())) throw BizException.forbidden("طلب لمنشأة أخرى");
    if (!Set.of("confirmed", "done", "cancelled").contains(r.status()))
      throw BizException.badRequest("status_invalid", "حالة غير معروفة");
    req.setStatus(r.status(), r.note());
    requests.save(req);
    audit.record("portal.request_status", "portal_service_request", String.valueOf(id),
        "{\"status\":\"" + r.status() + "\"}");
    return Map.of("request", req);
  }
}
