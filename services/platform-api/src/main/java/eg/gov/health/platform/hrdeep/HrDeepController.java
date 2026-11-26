package eg.gov.health.platform.hrdeep;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.*; import java.util.*;
/** Human resources, payroll & training (module 116). The
 *  employment file, appointment, contracts and transfers;
 *  attendance, leave, shifts and overtime; payroll, allowances
 *  and deductions; performance, competencies and development;
 *  training and continuing education; sanctions and grievances;
 *  and succession planning are managed. */
@RestController @RequestMapping("/api/hr-deep")
public class HrDeepController {
  private final EmployeeRecordRepository employees; private final TrainingRecordRepository trainings;
  private final GrievanceRepository grievances; private final AuditService audit;
  private final eg.gov.health.platform.workforce.StaffMemberRepository roster;
  private final eg.gov.health.platform.workforce.ShiftRepository shifts;
  public HrDeepController(EmployeeRecordRepository employees, TrainingRecordRepository trainings,
                          GrievanceRepository grievances, AuditService audit,
                          eg.gov.health.platform.workforce.StaffMemberRepository roster,
                          eg.gov.health.platform.workforce.ShiftRepository shifts) {
    this.employees=employees; this.trainings=trainings; this.grievances=grievances; this.audit=audit;
    this.roster=roster; this.shifts=shifts;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }

  public record EmployeeBody(String staffRef, String role, Boolean clinical, Boolean licenceValid) {}
  @PostMapping("/employees")
  Map<String,Object> employee(@RequestBody EmployeeBody r) {
    var p = principal();
    var e = employees.save(new EmployeeRecord(UUID.fromString(p.facilityId()), p.governorate(), r.staffRef(), r.role(),
      Boolean.TRUE.equals(r.clinical()), Boolean.TRUE.equals(r.licenceValid())));
    return Map.of("ok",true,"employee",e,"assignable",HrRules.assignable(e.isClinical(), e.isLicenceValid()),
      "note","a clinical role with a lapsed licence is not assignable — a licence is not a paperwork detail");
  }
  public record TrainingBody(String staffRef, String course, Boolean mandatory, Boolean completed, LocalDate dueDate) {}
  @PostMapping("/trainings")
  Map<String,Object> training(@RequestBody TrainingBody r) {
    var p = principal();
    var t = trainings.save(new TrainingRecord(UUID.fromString(p.facilityId()), p.governorate(), r.staffRef(), r.course(),
      Boolean.TRUE.equals(r.mandatory()), Boolean.TRUE.equals(r.completed()), r.dueDate()));
    boolean compliant = HrRules.trainingCompliant(Boolean.TRUE.equals(r.mandatory()), Boolean.TRUE.equals(r.completed()), r.dueDate(), LocalDate.now());
    return Map.of("ok",true,"training",t,"compliant",compliant);
  }
  public record GrievanceBody(String staffRef, String subject, Boolean reviewed, String outcome) {}
  @PostMapping("/grievances")
  Map<String,Object> grievance(@RequestBody GrievanceBody r) {
    var p = principal();
    var g = grievances.save(new Grievance(UUID.fromString(p.facilityId()), p.governorate(), r.staffRef(), r.subject(),
      Boolean.TRUE.equals(r.reviewed()), r.outcome()));
    return Map.of("ok",true,"grievance",g);
  }
  public record ReviewBody(String outcome) {}
  /** A grievance is reviewed with a recorded outcome before it
   *  can close — review is its own act, not a creation detail. */
  @PostMapping("/grievances/{id}/review")
  Map<String,Object> review(@PathVariable UUID id, @RequestBody ReviewBody r) {
    principal();
    var g = grievances.findById(id).orElseThrow(() -> BizException.notFound("grievance_not_found","التظلم غير موجود"));
    if ("closed".equals(g.getStatus())) throw BizException.conflict("already_closed","التظلم مغلق بالفعل");
    if (r.outcome() == null || r.outcome().isBlank())
      throw BizException.badRequest("outcome_required","نتيجة المراجعة مطلوبة ومسجلة");
    g.review(r.outcome()); grievances.save(g);
    audit.record("hr.grievance.reviewed","grievance",g.getId().toString(),"{}");
    return Map.of("ok",true,"grievance",g,"closable",HrRules.grievanceClosable(g.isReviewed(), g.getOutcome()));
  }
  /** Workforce operations board: who cannot be assigned (lapsed
   *  clinical licence), who is non-compliant (overdue mandatory
   *  training), what training is due, which grievances wait — and
   *  today's roster picture from the workforce shifts. */
  @GetMapping("/board")
  Map<String,Object> board() {
    var p = principal();
    UUID fid = UUID.fromString(p.facilityId());
    LocalDate today = LocalDate.now();
    var allTrainings = trainings.findByFacilityId(fid);
    Map<String,Integer> overdueByStaff = new HashMap<>();
    List<Map<String,Object>> trainingQueue = new ArrayList<>();
    int trainingsOverdue = 0, trainingsDueSoon = 0;
    for (var t : allTrainings) {
      String state = WorkforceBoardRules.trainingState(t.isMandatory(), t.isCompleted(), t.getDueDate(), today);
      if ("overdue".equals(state)) { trainingsOverdue++; overdueByStaff.merge(t.getStaffRef(), 1, Integer::sum); }
      if ("due_soon".equals(state)) trainingsDueSoon++;
      int rank = WorkforceBoardRules.trainingRank(state);
      if (rank == 9) continue;
      var row = new LinkedHashMap<String,Object>();
      row.put("id", t.getId()); row.put("staff_ref", t.getStaffRef()); row.put("course", t.getCourse());
      row.put("mandatory", t.isMandatory()); row.put("due_date", t.getDueDate()); row.put("state", state); row.put("rank", rank);
      trainingQueue.add(row);
    }
    trainingQueue.sort(Comparator.comparingInt((Map<String,Object> r) -> (Integer) r.get("rank")));
    List<Map<String,Object>> staffQueue = new ArrayList<>();
    int licenceBlocked = 0, staffTotal = 0, clinicalTotal = 0;
    for (var e : employees.findByFacilityId(fid)) {
      staffTotal++; if (e.isClinical()) clinicalTotal++;
      int overdue = overdueByStaff.getOrDefault(e.getStaffRef(), 0);
      String state = WorkforceBoardRules.employeeState(e.isClinical(), e.isLicenceValid(), overdue);
      if ("licence_blocked".equals(state)) licenceBlocked++;
      int rank = WorkforceBoardRules.employeeRank(state);
      if (rank == 9) continue;
      var row = new LinkedHashMap<String,Object>();
      row.put("id", e.getId()); row.put("staff_ref", e.getStaffRef()); row.put("role", e.getRole());
      row.put("clinical", e.isClinical()); row.put("licence_valid", e.isLicenceValid());
      row.put("assignable", HrRules.assignable(e.isClinical(), e.isLicenceValid()));
      row.put("overdue_trainings", overdue); row.put("state", state); row.put("rank", rank);
      staffQueue.add(row);
    }
    staffQueue.sort(Comparator.comparingInt((Map<String,Object> r) -> (Integer) r.get("rank")));
    List<Map<String,Object>> grievanceQueue = new ArrayList<>();
    int grievancesOpen = 0, grievancesUnreviewed = 0;
    for (var g : grievances.findByFacilityId(fid)) {
      int rank = WorkforceBoardRules.grievanceRank(g.getStatus(), g.isReviewed());
      if (rank == 9) continue;
      grievancesOpen++; if (!g.isReviewed()) grievancesUnreviewed++;
      var row = new LinkedHashMap<String,Object>();
      row.put("id", g.getId()); row.put("staff_ref", g.getStaffRef()); row.put("subject", g.getSubject());
      row.put("reviewed", g.isReviewed()); row.put("outcome", g.getOutcome());
      row.put("closable", HrRules.grievanceClosable(g.isReviewed(), g.getOutcome())); row.put("rank", rank);
      grievanceQueue.add(row);
    }
    grievanceQueue.sort(Comparator.comparingInt((Map<String,Object> r) -> (Integer) r.get("rank")));
    var rosterRows = roster.findByFacilityIdOrderByFullName(fid);
    long active = rosterRows.stream().filter(m -> "active".equals(m.getStatus())).count();
    long onLeave = rosterRows.stream().filter(m -> "on_leave".equals(m.getStatus())).count();
    long shiftsToday = shifts.findByFacilityIdOrderByStartsAtAsc(fid).stream()
        .filter(sh -> sh.getStartsAt() != null && sh.getStartsAt().toLocalDate().equals(today) && "scheduled".equals(sh.getStatus())).count();
    var summary = new LinkedHashMap<String,Object>();
    summary.put("staff_records", staffTotal); summary.put("clinical_staff", clinicalTotal);
    summary.put("licence_blocked", licenceBlocked);
    summary.put("trainings_overdue", trainingsOverdue); summary.put("trainings_due_soon", trainingsDueSoon);
    summary.put("grievances_open", grievancesOpen); summary.put("grievances_unreviewed", grievancesUnreviewed);
    summary.put("roster_members", rosterRows.size()); summary.put("roster_active", active);
    summary.put("roster_on_leave", onLeave); summary.put("shifts_today", shiftsToday);
    return Map.of("ok", true, "staff", staffQueue, "trainings", trainingQueue,
        "grievances", grievanceQueue, "summary", summary);
  }
  @PostMapping("/grievances/{id}/close")
  Map<String,Object> close(@PathVariable UUID id) {
    principal();
    var g = grievances.findById(id).orElseThrow(() -> BizException.notFound("grievance_not_found","التظلم غير موجود"));
    if (!HrRules.grievanceClosable(g.isReviewed(), g.getOutcome()))
      throw BizException.conflict("not_closable","تظلم لا يُغلق قبل مراجعته ونتيجة مسجلة — تظلم ينتهي بصمت يعلّم الجميع أن الكلام بلا جدوى");
    g.close(); grievances.save(g);
    return Map.of("ok",true,"grievance",g);
  }
}
