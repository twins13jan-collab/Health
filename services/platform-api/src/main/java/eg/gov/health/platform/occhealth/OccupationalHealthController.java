package eg.gov.health.platform.occhealth;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.*; import java.util.*;
/** Health of workers in high-risk sectors (module 144).
 *  Occupational screening and surveillance programmes with
 *  exposure management for substances, noise, heat and radiation.
 *  Clearance is computed, not assumed: an overdue periodic
 *  examination or an exposure incident without its post-exposure
 *  examination removes clearance for the exposure duty. Reports to
 *  the employer observe disclosure limits — fitness status and
 *  restrictions only, never the diagnosis. Prevention,
 *  vaccination and training run through the same record. */
@RestController @RequestMapping("/api/occupational-health")
public class OccupationalHealthController {
  private final WorkerHealthProfileRepository workers; private final OccupationalExaminationRepository exams;
  private final ExposureIncidentRepository incidents; private final OccupationalReferralRepository referrals; private final AuditService audit;
  public OccupationalHealthController(WorkerHealthProfileRepository workers, OccupationalExaminationRepository exams,
                                      ExposureIncidentRepository incidents, OccupationalReferralRepository referrals, AuditService audit) {
    this.workers=workers; this.exams=exams; this.incidents=incidents; this.referrals=referrals; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private WorkerHealthProfile worker(UUID id) {
    return workers.findById(id).orElseThrow(() -> BizException.notFound("worker_not_found","ملف العامل غير موجود"));
  }

  public record WorkerRequest(String workerCode, String sector, String riskLevel) {}
  @PostMapping("/workers")
  Map<String,Object> enroll(@RequestBody WorkerRequest r) {
    var p = principal();
    var w = workers.save(new WorkerHealthProfile(UUID.fromString(p.facilityId()), p.governorate(), r.workerCode(), r.sector(), r.riskLevel()));
    audit.record("occ.worker.enrolled","worker_health_profile",w.getId().toString(),"{}");
    return Map.of("ok",true,"worker",w);
  }
  public record ExamRequest(String examType, String exposureType, String result, String restrictions, String diagnosisNote,
                            LocalDate examinedAt, LocalDate nextDueAt) {}
  @PostMapping("/workers/{id}/examinations")
  Map<String,Object> examine(@PathVariable UUID id, @RequestBody ExamRequest r) {
    var p = principal(); worker(id);
    var e = exams.save(new OccupationalExamination(UUID.fromString(p.facilityId()), p.governorate(), id, r.examType(), r.exposureType(),
      r.result(), r.restrictions(), r.diagnosisNote(), r.examinedAt(), r.nextDueAt()));
    audit.record("occ.examination.recorded","occupational_examination",e.getId().toString(),"{}");
    return Map.of("ok",true,"examination",e);
  }
  @GetMapping("/workers/{id}/clearance")
  Map<String,Object> clearance(@PathVariable UUID id) {
    principal(); worker(id);
    var list = exams.findByWorkerIdOrderByExaminedAtDesc(id);
    var latest = list.isEmpty() ? null : list.get(0);
    boolean overdue = latest != null && OccHealthRules.examOverdue(latest.getNextDueAt(), LocalDate.now());
    boolean openExposure = incidents.existsByWorkerIdAndFollowUpExamDoneFalse(id);
    boolean cleared = latest != null && OccHealthRules.clearedForDuty(latest.getResult(), overdue, openExposure);
    return Map.of("ok",true,"worker_id",id.toString(),"cleared_for_high_risk_duty",cleared,
      "latest_result",latest == null ? "none" : String.valueOf(latest.getResult()),
      "exam_overdue",overdue,"open_exposure_without_follow_up",openExposure);
  }
  public record IncidentRequest(String exposureType, String doseOrLevel, OffsetDateTime occurredAt, String actionTaken) {}
  @PostMapping("/workers/{id}/exposure-incidents")
  Map<String,Object> incident(@PathVariable UUID id, @RequestBody IncidentRequest r) {
    var p = principal(); worker(id);
    var i = incidents.save(new ExposureIncident(UUID.fromString(p.facilityId()), p.governorate(), id, r.exposureType(), r.doseOrLevel(), r.occurredAt(), r.actionTaken()));
    audit.record("occ.exposure.recorded","exposure_incident",i.getId().toString(),"{}");
    return Map.of("ok",true,"incident",i,"effect","clearance suspended until the post-exposure examination is done");
  }
  @PostMapping("/exposure-incidents/{iid}/follow-up-done")
  Map<String,Object> followUp(@PathVariable UUID iid) {
    principal();
    var i = incidents.findById(iid).orElseThrow(() -> BizException.notFound("incident_not_found","حادث التعرض غير موجود"));
    i.completeFollowUp(); incidents.save(i);
    return Map.of("ok",true,"incident",i);
  }
  @GetMapping("/workers/{id}/employer-report")
  Map<String,Object> employerReport(@PathVariable UUID id) {
    principal(); worker(id);
    var list = exams.findByWorkerIdOrderByExaminedAtDesc(id);
    if (list.isEmpty()) throw BizException.notFound("no_examination","لا فحص مسجل بعد");
    var latest = list.get(0);
    // Disclosure limits: the employer receives fitness + restrictions. The diagnosis never leaves this service.
    return Map.of("ok",true,"employer_report",OccHealthRules.employerDisclosure(latest.getResult(), latest.getRestrictions()));
  }
  public record ReferralRequest(String reason) {}
  @PostMapping("/workers/{id}/referrals")
  Map<String,Object> refer(@PathVariable UUID id, @RequestBody ReferralRequest r) {
    var p = principal(); worker(id);
    var rf = referrals.save(new OccupationalReferral(UUID.fromString(p.facilityId()), p.governorate(), id, r.reason()));
    audit.record("occ.referral.created","occupational_referral",rf.getId().toString(),"{}");
    return Map.of("ok",true,"referral",rf);
  }
}
