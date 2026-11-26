package eg.gov.health.platform.clinical;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @RequestMapping("/api/encounters")
public class EncounterController {
  private final EncounterRepository repo; private final AuditService audit;
  private final eg.gov.health.platform.patient.PatientRepository patients;
  public EncounterController(EncounterRepository repo, AuditService audit,
                             eg.gov.health.platform.patient.PatientRepository patients) {
    this.repo = repo; this.audit = audit; this.patients = patients;
  }
  public record VitalsDto(Integer respiratoryRate, Integer spo2, Boolean onOxygen, Integer systolicBp, Integer pulse, Double temperature, String consciousness) {}
  public record EncounterRequest(UUID patientId, String type, String chiefComplaint, Integer triageLevel, VitalsDto vitals) {}
  @PostMapping
  Map<String, Object> create(@RequestBody EncounterRequest r) {
    var p = TenantContext.get();
    if (p == null || p.facilityId() == null) throw BizException.forbidden("يتطلب حساب منشأة لإنشاء زيارة");
    if (r.patientId() == null) throw BizException.badRequest("missing_field", "patientId مطلوب");
    Integer news2 = null; News2Calculator.Score score = null;
    if (r.vitals() != null && r.vitals().respiratoryRate() != null) {
      var v = r.vitals();
      score = News2Calculator.score(new News2Calculator.Vitals(v.respiratoryRate(), v.spo2() == null ? 98 : v.spo2(),
        Boolean.TRUE.equals(v.onOxygen()), v.systolicBp() == null ? 120 : v.systolicBp(), v.pulse() == null ? 80 : v.pulse(),
        v.temperature() == null ? 37.0 : v.temperature(), v.consciousness()));
      news2 = score.total();
    }
    var e = repo.save(new Encounter(UUID.fromString(p.facilityId()), p.governorate(), r.patientId(), r.type(), r.chiefComplaint(), r.triageLevel(), news2));
    audit.record("encounter.created", "encounter", e.getId().toString(), "{}");
    var out = new java.util.HashMap<String, Object>(); out.put("ok", true); out.put("encounter", e);
    if (score != null) { out.put("news2", score); out.put("advisory", "NEWS2 دعم قرار فقط — القرار السريري للطبيب المعالج"); }
    return out;
  }
  @GetMapping
  Map<String, Object> list(@RequestParam(required = false) UUID patientId, @RequestParam(required = false) Integer page, @RequestParam(required = false) Integer size) {
    var p = TenantContext.get();
    var rows = patientId != null ? repo.findByPatientIdOrderByArrivedAtDesc(patientId)
      : repo.findByFacilityIdOrderByArrivedAtDesc(UUID.fromString(p.facilityId()));
    return Paging.page(rows, "encounters", page, size);
  }
  @GetMapping("/{id}")
  Encounter get(@PathVariable UUID id) { return repo.findById(id).orElseThrow(() -> BizException.notFound("encounter_not_found", "الزيارة غير موجودة")); }
  @PostMapping("/{id}/status")
  Map<String, Object> status(@PathVariable UUID id, @RequestBody Map<String, String> body) {
    var e = get(id); String s = body.getOrDefault("status", "");
    if (!List.of("open", "in_progress", "completed", "closed", "cancelled").contains(s))
      throw BizException.badRequest("invalid_status", "حالة غير مسموحة");
    e.setStatus(s); repo.save(e);
    audit.record("encounter.status." + s, "encounter", id.toString(), "{}");
    return Map.of("ok", true, "encounter", e);
  }
  public record TriageRequest(Integer triageLevel, String chiefComplaint, VitalsDto vitals) {}
  /** (Re-)triage an arrival: set the five-level priority and recompute
   *  NEWS2 from fresh vitals when supplied. Triage is a clinical act —
   *  it is audited with the level it set. */
  @PostMapping("/{id}/triage")
  Map<String, Object> triage(@PathVariable UUID id, @RequestBody TriageRequest r) {
    var e = get(id);
    if (!ErRules.validLevel(r.triageLevel()))
      throw BizException.badRequest("invalid_triage_level", "مستوى الفرز يجب أن يكون من 1 إلى 5");
    Integer news2 = null; News2Calculator.Score score = null;
    if (r.vitals() != null && r.vitals().respiratoryRate() != null) {
      var v = r.vitals();
      score = News2Calculator.score(new News2Calculator.Vitals(v.respiratoryRate(), v.spo2() == null ? 98 : v.spo2(),
        Boolean.TRUE.equals(v.onOxygen()), v.systolicBp() == null ? 120 : v.systolicBp(), v.pulse() == null ? 80 : v.pulse(),
        v.temperature() == null ? 37.0 : v.temperature(), v.consciousness()));
      news2 = score.total();
    }
    e.applyTriage(r.triageLevel(), news2, r.chiefComplaint());
    repo.save(e);
    audit.record("encounter.triage." + r.triageLevel(), "encounter", id.toString(), "{}");
    var out = new HashMap<String, Object>(); out.put("ok", true); out.put("encounter", e);
    if (score != null) out.put("news2", score);
    return out;
  }
  /** The ER board for this facility: today's emergency arrivals as a
   *  priority queue with live wait times and target-breach flags. */
  @GetMapping("/er-board")
  Map<String, Object> erBoard() {
    var p = TenantContext.get();
    if (p == null || p.facilityId() == null) throw BizException.forbidden("يتطلب حساب منشأة");
    var now = java.time.OffsetDateTime.now();
    var startOfDay = now.toLocalDate().atStartOfDay(now.getOffset()).toOffsetDateTime();
    var rows = repo.findByFacilityIdOrderByArrivedAtDesc(UUID.fromString(p.facilityId())).stream()
      .filter(e -> "emergency".equals(e.getType()) && !e.getArrivedAt().isBefore(startOfDay))
      .map(e -> {
        var m = new LinkedHashMap<String, Object>();
        m.put("id", e.getId()); m.put("patientId", e.getPatientId());
        patients.findById(e.getPatientId()).ifPresent(pt -> { m.put("patientName", pt.getFullName()); m.put("nationalId", pt.getNationalId()); });
        m.put("chiefComplaint", e.getChiefComplaint()); m.put("triageLevel", e.getTriageLevel());
        m.put("news2Score", e.getNews2Score()); m.put("status", e.getStatus()); m.put("arrivedAt", e.getArrivedAt());
        long wait = ErRules.waitMinutes(e.getArrivedAt(), now);
        m.put("waitMinutes", wait);
        m.put("targetMinutes", e.getTriageLevel() == null ? null : ErRules.targetMinutes(e.getTriageLevel()));
        m.put("breached", ErRules.breached(e.getTriageLevel(), wait, e.getStatus()));
        return m;
      }).collect(java.util.stream.Collectors.toList());
    rows.sort(ErRules.queueOrder());
    long waiting = rows.stream().filter(r -> "open".equals(r.get("status"))).count();
    long inTreatment = rows.stream().filter(r -> "in_progress".equals(r.get("status"))).count();
    long breaches = rows.stream().filter(r -> Boolean.TRUE.equals(r.get("breached"))).count();
    long longest = rows.stream().filter(r -> "open".equals(r.get("status")))
      .mapToLong(r -> ((Number) r.get("waitMinutes")).longValue()).max().orElse(0);
    var summary = new LinkedHashMap<String, Object>();
    summary.put("waiting", waiting); summary.put("inTreatment", inTreatment);
    summary.put("seenToday", rows.size()); summary.put("breaches", breaches); summary.put("longestWaitMinutes", longest);
    return Map.of("ok", true, "queue", rows, "summary", summary);
  }
}
