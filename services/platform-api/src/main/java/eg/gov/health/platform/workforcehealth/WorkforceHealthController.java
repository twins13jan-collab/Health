package eg.gov.health.platform.workforcehealth;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.*; import java.util.*;
/** Occupational health & worker safety (module 56). Periodic
 *  examinations, occupational fitness and vaccinations for
 *  health workers; exposure to blood, infection, chemicals,
 *  radiation, gases and biological hazards; occupational
 *  injuries with investigation, follow-up, reporting and
 *  compensation; PPE with training and issue records; and
 *  confidential psychological support and burnout care. */
@RestController @RequestMapping("/api/workforce-health")
public class WorkforceHealthController {
  private final WorkerHealthRecordRepository records; private final WorkerExposureIncidentRepository incidents;
  private final PpeIssueRepository ppe; private final WorkerSupportRecordRepository support; private final AuditService audit;
  public WorkforceHealthController(WorkerHealthRecordRepository records, WorkerExposureIncidentRepository incidents,
                                   PpeIssueRepository ppe, WorkerSupportRecordRepository support, AuditService audit) {
    this.records=records; this.incidents=incidents; this.ppe=ppe; this.support=support; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }

  public record RecordBody(String workerRef, String workerRole, LocalDate lastPeriodicExamDate, Boolean vaccinationsUpToDate) {}
  @PostMapping("/records")
  Map<String,Object> record(@RequestBody RecordBody r) {
    var p = principal();
    var rec = records.save(new WorkerHealthRecord(UUID.fromString(p.facilityId()), p.governorate(), r.workerRef(), r.workerRole(),
      r.lastPeriodicExamDate(), Boolean.TRUE.equals(r.vaccinationsUpToDate())));
    return Map.of("ok",true,"record",rec,"fit_for_exposure_duties",
      WorkerHealthRules.fitForExposureDuties(rec.getLastPeriodicExamDate(), rec.isVaccinationsUpToDate(), LocalDate.now()));
  }
  @GetMapping("/records/{id}/fitness")
  Map<String,Object> fitness(@PathVariable UUID id) {
    principal();
    var rec = records.findById(id).orElseThrow(() -> BizException.notFound("record_not_found","السجل غير موجود"));
    boolean fit = WorkerHealthRules.fitForExposureDuties(rec.getLastPeriodicExamDate(), rec.isVaccinationsUpToDate(), LocalDate.now());
    return Map.of("ok",true,"record_id",id.toString(),"fit_for_exposure_duties",fit,
      "rule","periodic exam within 12 months AND vaccinations current — expired fitness is not fitness");
  }
  public record IncidentBody(String workerRef, String exposureType) {}
  @PostMapping("/exposure-incidents")
  Map<String,Object> incident(@RequestBody IncidentBody r) {
    var p = principal();
    var i = incidents.save(new WorkerExposureIncident(UUID.fromString(p.facilityId()), p.governorate(), r.workerRef(), r.exposureType()));
    audit.record("worker.exposure.reported","worker_exposure_incident",i.getId().toString(),"{}");
    return Map.of("ok",true,"incident",i);
  }
  @PostMapping("/exposure-incidents/{id}/investigate")
  Map<String,Object> investigate(@PathVariable UUID id) {
    principal();
    var i = incidents.findById(id).orElseThrow(() -> BizException.notFound("incident_not_found","الحادث غير موجود"));
    i.investigate(); incidents.save(i);
    return Map.of("ok",true,"incident",i);
  }
  @PostMapping("/exposure-incidents/{id}/follow-up")
  Map<String,Object> followUp(@PathVariable UUID id) {
    principal();
    var i = incidents.findById(id).orElseThrow(() -> BizException.notFound("incident_not_found","الحادث غير موجود"));
    i.followUp(); incidents.save(i);
    return Map.of("ok",true,"incident",i);
  }
  @PostMapping("/exposure-incidents/{id}/close")
  Map<String,Object> closeIncident(@PathVariable UUID id) {
    principal();
    var i = incidents.findById(id).orElseThrow(() -> BizException.notFound("incident_not_found","الحادث غير موجود"));
    if (!WorkerHealthRules.incidentClosable(i.isInvestigationDone(), i.isFollowUpDone()))
      throw BizException.conflict("closure_blocked","حادث تعرض عامل لا يُغلق قبل التحقيق والمتابعة الطبية");
    i.close(); incidents.save(i);
    return Map.of("ok",true,"incident",i);
  }
  public record PpeBody(String workerRef, String item, Boolean trainingDone) {}
  @PostMapping("/ppe-issues")
  Map<String,Object> ppe(@RequestBody PpeBody r) {
    var p = principal();
    var issue = ppe.save(new PpeIssue(UUID.fromString(p.facilityId()), p.governorate(), r.workerRef(), r.item(), Boolean.TRUE.equals(r.trainingDone())));
    audit.record("worker.ppe.issued","ppe_issue",issue.getId().toString(),"{}");
    return Map.of("ok",true,"issue",issue,"compliant",WorkerHealthRules.ppeCompliant(Boolean.TRUE.equals(r.trainingDone())),
      "note","PPE issued without training is costume, not protection — flagged non-compliant");
  }
  public record SupportBody(String workerRef, String supportType, String notes) {}
  @PostMapping("/support-records")
  Map<String,Object> support(@RequestBody SupportBody r) {
    var p = principal();
    if (Set.of("receptionist").contains(p.role()))
      throw BizException.forbidden("سجلات الدعم النفسي والاحتراق سرية — خلف جدران الأدوار المختصة فقط");
    var s = support.save(new WorkerSupportRecord(UUID.fromString(p.facilityId()), p.governorate(), r.workerRef(), r.supportType(), r.notes()));
    audit.record("worker.support.recorded","worker_support_record",s.getId().toString(),"{}");
    return Map.of("ok",true,"support_record",s,"confidentiality","psychological support records are confidential to the occupational-health team");
  }
}
