package eg.gov.health.platform.surgeryops;
import eg.gov.health.platform.clinical.*;
import eg.gov.health.platform.common.*;
import eg.gov.health.platform.patient.*;
import org.springframework.web.bind.annotation.*;
import java.time.*;
import java.util.*;

/** Operating theatres & anaesthesia (Phase 4.5): rooms as managed
 *  resources with a day board, the prioritised surgical waiting
 *  list, case flow (start / complete / postpone with reason), and
 *  the anaesthesia record that gates starting a case. The WHO
 *  checklist itself stays in SurgeryController (cycle 220). */
@RestController @RequestMapping("/api/or")
public class OrController {
  private static final ZoneId CAIRO = ZoneId.of("Africa/Cairo");
  private final OperatingRoomRepository rooms;
  private final SurgicalWaitlistRepository waitlist;
  private final AnaesthesiaRecordRepository anaesthesia;
  private final SurgicalCaseRepository cases;
  private final PatientRepository patients;
  private final AuditService audit;

  public OrController(OperatingRoomRepository rooms, SurgicalWaitlistRepository waitlist,
                      AnaesthesiaRecordRepository anaesthesia, SurgicalCaseRepository cases,
                      PatientRepository patients, AuditService audit) {
    this.rooms = rooms; this.waitlist = waitlist; this.anaesthesia = anaesthesia;
    this.cases = cases; this.patients = patients; this.audit = audit;
  }

  private TenantContext.Principal me() {
    var p = TenantContext.get();
    if (p == null || p.facilityId() == null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private UUID facilityId() { return UUID.fromString(me().facilityId()); }
  private String patientName(UUID id) { return patients.findById(id).map(pt -> pt.getFullName()).orElse("—"); }
  private SurgicalCase ownCase(UUID id) {
    var c = cases.findById(id).orElseThrow(() -> BizException.notFound("case_not_found", "الحالة غير موجودة"));
    if (!facilityId().equals(c.getFacilityId())) throw BizException.forbidden("حالة لمنشأة أخرى");
    return c;
  }
  private Map<String, Object> caseView(SurgicalCase c) {
    Map<String, Object> v = new LinkedHashMap<>();
    v.put("case", c);
    v.put("patientName", patientName(c.getPatientId()));
    v.put("anaesthesia", anaesthesia.findByCaseId(c.getId()).orElse(null));
    return v;
  }

  // ---------------------------------------------------------- rooms
  public record RoomRequest(String nameAr, String nameEn, String specialty) {}
  @PostMapping("/rooms")
  Map<String, Object> createRoom(@RequestBody RoomRequest r) {
    var p = me();
    if (r.nameAr() == null || r.nameAr().isBlank()) throw BizException.badRequest("room_name_required", "اسم الغرفة مطلوب");
    var room = rooms.save(new OperatingRoom(facilityId(), p.governorate(), r.nameAr(), r.nameEn(), r.specialty()));
    audit.record("or.room_created", "operating_room", String.valueOf(room.getId()), "{}");
    return Map.of("room", room);
  }
  @PostMapping("/rooms/{id}/deactivate")
  Map<String, Object> deactivateRoom(@PathVariable UUID id) {
    var room = rooms.findById(id).orElseThrow(() -> BizException.notFound("room_not_found", "الغرفة غير موجودة"));
    if (!facilityId().equals(room.getFacilityId())) throw BizException.forbidden("غرفة لمنشأة أخرى");
    room.setActive(false); rooms.save(room);
    return Map.of("room", room);
  }

  // ---------------------------------------------------------- board
  @GetMapping("/board")
  Map<String, Object> board(@RequestParam(required = false) LocalDate date) {
    LocalDate d = date == null ? LocalDate.now(CAIRO) : date;
    var dayCases = cases.findByFacilityIdOrderByScheduledAtAsc(facilityId()).stream()
        .filter(c -> c.getScheduledAt() != null && c.getScheduledAt().toLocalDate().equals(d))
        .toList();
    List<Map<String, Object>> roomViews = new ArrayList<>();
    for (var room : rooms.findByFacilityIdOrderByNameArAsc(facilityId())) {
      Map<String, Object> rv = new LinkedHashMap<>();
      rv.put("room", room);
      rv.put("cases", dayCases.stream().filter(c -> room.getId().equals(c.getRoomId())).map(this::caseView).toList());
      roomViews.add(rv);
    }
    var unassigned = dayCases.stream().filter(c -> c.getRoomId() == null).map(this::caseView).toList();
    long waiting = waitlist.findByFacilityIdAndStatusOrderByCreatedAtAsc(facilityId(), "waiting").size();
    return Map.of("date", String.valueOf(d), "rooms", roomViews, "unassigned", unassigned,
        "casesToday", dayCases.size(), "waitingCount", waiting);
  }

  // ---------------------------------------------------------- waitlist
  @GetMapping("/waitlist")
  Map<String, Object> waitlist() {
    var rows = waitlist.findByFacilityIdAndStatusOrderByCreatedAtAsc(facilityId(), "waiting");
    rows.sort(OrRules.waitlistOrder());
    var views = rows.stream().map(e -> {
      Map<String, Object> v = new LinkedHashMap<>();
      v.put("entry", e);
      v.put("patientName", patientName(e.getPatientId()));
      v.put("waitingDays", Math.max(0, java.time.Duration.between(e.getCreatedAt(), OffsetDateTime.now()).toDays()));
      return v;
    }).toList();
    return Map.of("waitlist", views, "count", views.size());
  }

  public record WaitlistRequest(UUID patientId, String procedureName, String specialty, String priority, String notes) {}
  @PostMapping("/waitlist")
  Map<String, Object> addToWaitlist(@RequestBody WaitlistRequest r) {
    var p = me();
    if (r.patientId() == null || r.procedureName() == null || r.procedureName().isBlank())
      throw BizException.badRequest("waitlist_fields", "المريض والإجراء مطلوبان");
    patients.findById(r.patientId()).orElseThrow(() -> BizException.notFound("patient_not_found", "المريض غير موجود في السجل الموحد"));
    String priority = r.priority() == null ? "elective" : r.priority();
    if (!Set.of("emergency", "urgent", "elective").contains(priority))
      throw BizException.badRequest("priority_invalid", "أولوية غير معروفة: " + priority);
    var e = waitlist.save(new SurgicalWaitlistEntry(facilityId(), p.governorate(), r.patientId(),
        r.procedureName(), r.specialty(), priority, p.email(), r.notes()));
    audit.record("or.waitlist_added", "surgical_waitlist", String.valueOf(e.getId()), "{\"priority\":\"" + priority + "\"}");
    return Map.of("entry", e, "patientName", patientName(e.getPatientId()));
  }

  public record ScheduleRequest(UUID roomId, OffsetDateTime scheduledAt) {}
  @PostMapping("/waitlist/{id}/schedule")
  Map<String, Object> schedule(@PathVariable UUID id, @RequestBody ScheduleRequest r) {
    var p = me();
    var e = waitlist.findById(id).orElseThrow(() -> BizException.notFound("entry_not_found", "قيد الانتظار غير موجود"));
    if (!facilityId().equals(e.getFacilityId())) throw BizException.forbidden("قيد لمنشأة أخرى");
    if (!"waiting".equals(e.getStatus())) throw BizException.conflict("entry_not_waiting", "هذا القيد ليس في الانتظار");
    if (r.roomId() == null || r.scheduledAt() == null) throw BizException.badRequest("schedule_fields", "الغرفة والموعد مطلوبان");
    var room = rooms.findById(r.roomId()).orElseThrow(() -> BizException.notFound("room_not_found", "الغرفة غير موجودة"));
    if (!facilityId().equals(room.getFacilityId())) throw BizException.forbidden("غرفة لمنشأة أخرى");
    if (!room.isActive()) throw BizException.conflict("room_inactive", "الغرفة موقوفة");
    var sameRoomStarts = cases.findByFacilityIdOrderByScheduledAtAsc(facilityId()).stream()
        .filter(c -> room.getId().equals(c.getRoomId()) && !"cancelled".equals(c.getStatus()) && !"postponed".equals(c.getStatus()))
        .map(SurgicalCase::getScheduledAt).toList();
    if (OrRules.roomConflict(r.scheduledAt(), sameRoomStarts))
      throw BizException.conflict("room_conflict", "تعارض في الغرفة: حالة أخرى ضمن نافذة التهيئة (60 دقيقة)");
    var c = new SurgicalCase(facilityId(), p.governorate(), e.getPatientId(), null,
        e.getProcedureName(), room.getNameAr(), r.scheduledAt());
    c.setRoomId(room.getId());
    cases.save(c);
    e.markScheduled(c.getId());
    waitlist.save(e);
    audit.record("or.case_scheduled", "surgical_case", String.valueOf(c.getId()),
        "{\"room\":\"" + room.getNameAr() + "\"}");
    return Map.of("case", caseView(c), "entry", e);
  }

  public record ReasonRequest(String reason) {}
  @PostMapping("/waitlist/{id}/cancel")
  Map<String, Object> cancelWaitlist(@PathVariable UUID id, @RequestBody ReasonRequest r) {
    var e = waitlist.findById(id).orElseThrow(() -> BizException.notFound("entry_not_found", "قيد الانتظار غير موجود"));
    if (!facilityId().equals(e.getFacilityId())) throw BizException.forbidden("قيد لمنشأة أخرى");
    String problem = OrRules.reasonProblem(r.reason());
    if (problem != null) throw BizException.badRequest("reason_required", problem);
    e.cancel(r.reason());
    waitlist.save(e);
    audit.record("or.waitlist_cancelled", "surgical_waitlist", String.valueOf(id), "{}");
    return Map.of("entry", e);
  }

  // ---------------------------------------------------------- case flow + anaesthesia
  @PostMapping("/cases/{id}/start")
  Map<String, Object> startCase(@PathVariable UUID id) {
    var c = ownCase(id);
    var rec = anaesthesia.findByCaseId(id);
    String priority = waitlist.findByScheduledCaseId(id).map(SurgicalWaitlistEntry::getPriority).orElse("elective");
    String problem = OrRules.startProblem(c.getStatus(), rec.isPresent(),
        rec.map(AnaesthesiaRecord::isPreopAssessment).orElse(false), priority);
    if (problem != null) throw BizException.badRequest("start_blocked", problem);
    c.setStatus("in_progress");
    cases.save(c);
    rec.ifPresent(r -> { r.start(); anaesthesia.save(r); });
    audit.record("or.case_started", "surgical_case", String.valueOf(id), "{}");
    return Map.of("case", caseView(c));
  }

  @PostMapping("/cases/{id}/complete")
  Map<String, Object> completeCase(@PathVariable UUID id) {
    var c = ownCase(id);
    if (!"in_progress".equals(c.getStatus())) throw BizException.conflict("case_not_in_progress", "الحالة ليست جارية");
    c.advanceTo("completed");
    cases.save(c);
    anaesthesia.findByCaseId(id).ifPresent(r -> { r.end(); anaesthesia.save(r); });
    waitlist.findByScheduledCaseId(id).ifPresent(e -> { e.markDone(); waitlist.save(e); });
    audit.record("or.case_completed", "surgical_case", String.valueOf(id), "{}");
    return Map.of("case", caseView(c));
  }

  @PostMapping("/cases/{id}/postpone")
  Map<String, Object> postponeCase(@PathVariable UUID id, @RequestBody ReasonRequest r) {
    var c = ownCase(id);
    String problem = OrRules.reasonProblem(r.reason());
    if (problem != null) throw BizException.badRequest("reason_required", problem);
    if (!"scheduled".equals(c.getStatus())) throw BizException.conflict("case_not_scheduled", "الحالة ليست مجدولة");
    c.setStatus("postponed");
    c.setStatusReason(r.reason());
    cases.save(c);
    audit.record("or.case_postponed", "surgical_case", String.valueOf(id), "{}");
    return Map.of("case", caseView(c));
  }

  public record AnaesthesiaRequest(Integer asaClass, String anaesthesiaType, String anaesthetist,
                                   Boolean preopAssessment, String complications, String notes) {}
  @PostMapping("/cases/{id}/anaesthesia")
  Map<String, Object> upsertAnaesthesia(@PathVariable UUID id, @RequestBody AnaesthesiaRequest r) {
    var c = ownCase(id);
    String problem = OrRules.anaesthesiaProblem(r.asaClass(), r.anaesthesiaType());
    if (problem != null) throw BizException.badRequest("anaesthesia_rule", problem);
    var rec = anaesthesia.findByCaseId(id).orElseGet(() -> new AnaesthesiaRecord(facilityId(), id, c.getPatientId()));
    rec.update(r.asaClass(), r.anaesthesiaType(), r.anaesthetist(), r.preopAssessment(), r.complications(), r.notes());
    anaesthesia.save(rec);
    audit.record("or.anaesthesia_updated", "surgical_case", String.valueOf(id), "{}");
    return Map.of("anaesthesia", rec);
  }

  @GetMapping("/cases/{id}")
  Map<String, Object> caseDetail(@PathVariable UUID id) {
    return Map.of("case", caseView(ownCase(id)));
  }
}
