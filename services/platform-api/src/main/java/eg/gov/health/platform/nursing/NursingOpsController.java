package eg.gov.health.platform.nursing;
import eg.gov.health.platform.adt.*;
import eg.gov.health.platform.clinical.News2Calculator;
import eg.gov.health.platform.common.*;
import eg.gov.health.platform.patient.*;
import eg.gov.health.platform.pharmacy.*;
import org.springframework.web.bind.annotation.*;
import java.time.*;
import java.util.*;

/** Ward nursing operations (Phase 4.4): the inpatient census as a
 *  nursing worklist, the medication administration record (MAR)
 *  generated from active prescriptions, and observation rounds
 *  with server-computed NEWS2. Assessments, care plans, tasks and
 *  handovers live in NursingController; this controller adds the
 *  medication and observation backbone and ties it to ADT. */
@RestController @RequestMapping("/api/nursing")
public class NursingOpsController {
  private static final ZoneId CAIRO = ZoneId.of("Africa/Cairo");
  private final InpatientAdmissionRepository admissions;
  private final WardRepository wards;
  private final PatientRepository patients;
  private final PrescriptionRepository prescriptions;
  private final MedAdministrationRepository mar;
  private final NursingRoundRepository rounds;
  private final NursingTaskRepository tasks;
  private final AuditService audit;

  public NursingOpsController(InpatientAdmissionRepository admissions, WardRepository wards,
                              PatientRepository patients, PrescriptionRepository prescriptions,
                              MedAdministrationRepository mar, NursingRoundRepository rounds,
                              NursingTaskRepository tasks, AuditService audit) {
    this.admissions = admissions; this.wards = wards; this.patients = patients;
    this.prescriptions = prescriptions; this.mar = mar; this.rounds = rounds;
    this.tasks = tasks; this.audit = audit;
  }

  private TenantContext.Principal me() {
    var p = TenantContext.get();
    if (p == null || p.facilityId() == null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private UUID facilityId() { return UUID.fromString(me().facilityId()); }
  private InpatientAdmission admission(UUID id) {
    var a = admissions.findById(id).orElseThrow(() -> BizException.notFound("admission_not_found", "تنويم غير موجود"));
    if (!a.getFacilityId().equals(facilityId())) throw BizException.forbidden("تنويم لمنشأة أخرى");
    return a;
  }
  private String patientName(UUID patientId) {
    return patients.findById(patientId).map(p -> p.getFullName()).orElse("—");
  }
  private Map<String, Object> marView(MedAdministration m, OffsetDateTime now) {
    Map<String, Object> v = new LinkedHashMap<>();
    v.put("administration", m);
    v.put("patientName", patientName(m.getPatientId()));
    v.put("state", MarRules.doseState(m.getScheduledAt(), m.getStatus(), now));
    return v;
  }

  // ---------------------------------------------------------- census
  @GetMapping("/census")
  Map<String, Object> census() {
    var p = me();
    OffsetDateTime now = OffsetDateTime.now();
    LocalDate today = LocalDate.now(CAIRO);
    OffsetDateTime dayStart = today.atStartOfDay(CAIRO).toOffsetDateTime();
    OffsetDateTime dayEnd = today.plusDays(1).atStartOfDay(CAIRO).toOffsetDateTime();
    var todays = mar.findByFacilityIdAndScheduledAtBetweenOrderByScheduledAtAsc(facilityId(), dayStart, dayEnd);
    Map<UUID, List<MedAdministration>> byAdmission = new HashMap<>();
    for (var m : todays) if (m.getAdmissionId() != null) byAdmission.computeIfAbsent(m.getAdmissionId(), k -> new ArrayList<>()).add(m);
    List<Map<String, Object>> rows = new ArrayList<>();
    int dueTotal = 0, overdueTotal = 0;
    for (var a : admissions.findByFacilityIdAndStatusOrderByAdmittedAtAsc(facilityId(), "admitted")) {
      var doses = byAdmission.getOrDefault(a.getId(), List.of());
      long due = doses.stream().filter(d -> "due".equals(MarRules.doseState(d.getScheduledAt(), d.getStatus(), now))).count();
      long overdue = doses.stream().filter(d -> "overdue".equals(MarRules.doseState(d.getScheduledAt(), d.getStatus(), now))).count();
      dueTotal += due; overdueTotal += overdue;
      var lastRound = rounds.findFirstByAdmissionIdOrderByRoundAtDesc(a.getId());
      Map<String, Object> row = new LinkedHashMap<>();
      row.put("admission", a);
      row.put("patientName", patientName(a.getPatientId()));
      row.put("wardName", wards.findById(a.getWardId()).map(Ward::getNameAr).orElse("—"));
      row.put("dueDoses", due);
      row.put("overdueDoses", overdue);
      row.put("openTasks", tasks.findByPatientRefAndStatus(a.getPatientId().toString(), "open").size());
      row.put("lastNews2", lastRound.map(NursingRound::getNews2Total).orElse(null));
      row.put("lastRoundAt", lastRound.map(NursingRound::getRoundAt).orElse(null));
      rows.add(row);
    }
    return Map.of("census", rows, "count", rows.size(), "dueDoses", dueTotal, "overdueDoses", overdueTotal);
  }

  // ---------------------------------------------------------- MAR
  public record GenerateRequest(UUID admissionId, LocalDate date) {}
  @PostMapping("/mar/generate")
  Map<String, Object> generate(@RequestBody GenerateRequest req) {
    var p = me();
    var a = admission(req.admissionId());
    LocalDate date = req.date() == null ? LocalDate.now(CAIRO) : req.date();
    var active = prescriptions.findByPatientIdAndStatusIn(a.getPatientId(), List.of("prescribed", "verified", "dispensed"));
    int created = 0;
    for (var rx : active) {
      for (var time : MarRules.timesFor(rx.getFrequency())) {
        OffsetDateTime at = OffsetDateTime.of(LocalDateTime.of(date, time), CAIRO.getRules().getOffset(LocalDateTime.of(date, time)));
        if (mar.existsByPrescriptionIdAndScheduledAt(rx.getId(), at)) continue;
        mar.save(new MedAdministration(facilityId(), p.governorate(), a.getPatientId(), a.getId(),
            rx.getId(), rx.getDrugName() != null ? rx.getDrugName() : rx.getDrugCode(), rx.getDose(), at));
        created++;
      }
    }
    audit.record("nursing.mar_generated", "admission", String.valueOf(a.getId()),
        "{\"date\":\"" + date + "\",\"created\":" + created + "}");
    return Map.of("created", created, "date", String.valueOf(date),
        "mar", mar.findByAdmissionIdOrderByScheduledAtAsc(a.getId()).stream()
            .filter(m -> m.getScheduledAt().toLocalDate().equals(date)).map(m -> marView(m, OffsetDateTime.now())).toList());
  }

  @GetMapping("/mar")
  Map<String, Object> marForAdmission(@RequestParam UUID admissionId, @RequestParam(required = false) LocalDate date) {
    admission(admissionId);
    LocalDate d = date == null ? LocalDate.now(CAIRO) : date;
    var rows = mar.findByAdmissionIdOrderByScheduledAtAsc(admissionId).stream()
        .filter(m -> m.getScheduledAt().toLocalDate().equals(d)).map(m -> marView(m, OffsetDateTime.now())).toList();
    return Map.of("mar", rows, "count", rows.size(), "date", String.valueOf(d));
  }

  public record RecordRequest(String status, String reason, String note) {}
  @PostMapping("/mar/{id}/record")
  Map<String, Object> record(@PathVariable UUID id, @RequestBody RecordRequest req) {
    var p = me();
    var m = mar.findById(id).orElseThrow(() -> BizException.notFound("dose_not_found", "جرعة غير موجودة"));
    if (!m.getFacilityId().equals(facilityId())) throw BizException.forbidden("جرعة لمنشأة أخرى");
    if (!"scheduled".equals(m.getStatus())) throw BizException.conflict("dose_already_recorded", "هذه الجرعة سُجّلت من قبل");
    String problem = MarRules.administrationProblem(req.status(), req.reason());
    if (problem != null) throw BizException.badRequest("mar_rule", problem);
    m.record(req.status(), req.reason(), req.note(), p.email());
    mar.save(m);
    audit.record("nursing.dose_" + req.status(), "med_administration", String.valueOf(id),
        "{\"drug\":\"" + m.getDrugName() + "\"}");
    return Map.of("administration", m, "state", MarRules.doseState(m.getScheduledAt(), m.getStatus(), OffsetDateTime.now()));
  }

  // ---------------------------------------------------------- rounds
  public record RoundRequest(UUID admissionId, Integer respiratoryRate, Integer spo2, Boolean onOxygen,
                             Integer systolicBp, Integer pulse, Double temperature, String consciousness,
                             Integer painScore, String mobility, String note) {}
  @PostMapping("/rounds")
  Map<String, Object> recordRound(@RequestBody RoundRequest req) {
    var p = me();
    var a = admission(req.admissionId());
    String painProblem = MarRules.painProblem(req.painScore());
    if (painProblem != null) throw BizException.badRequest("pain_rule", painProblem);
    Integer news2 = null; String band = null;
    if (req.respiratoryRate() != null && req.spo2() != null && req.systolicBp() != null
        && req.pulse() != null && req.temperature() != null) {
      var s = News2Calculator.score(new News2Calculator.Vitals(req.respiratoryRate(), req.spo2(),
          Boolean.TRUE.equals(req.onOxygen()), req.systolicBp(), req.pulse(), req.temperature(),
          req.consciousness() == null ? "alert" : req.consciousness()));
      news2 = s.total(); band = s.riskBand();
    }
    var r = rounds.save(new NursingRound(facilityId(), p.governorate(), a.getPatientId(), a.getId(),
        req.respiratoryRate(), req.spo2(), Boolean.TRUE.equals(req.onOxygen()), req.systolicBp(),
        req.pulse(), req.temperature(), req.consciousness(), req.painScore(), req.mobility(),
        news2, band, req.note(), p.email()));
    audit.record("nursing.round_recorded", "admission", String.valueOf(a.getId()),
        "{\"news2\":" + news2 + "}");
    return Map.of("round", r);
  }

  @GetMapping("/rounds")
  Map<String, Object> roundsFor(@RequestParam UUID admissionId) {
    admission(admissionId);
    var rows = rounds.findByAdmissionIdOrderByRoundAtDesc(admissionId);
    return Map.of("rounds", rows, "count", rows.size());
  }
}
