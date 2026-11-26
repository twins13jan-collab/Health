package eg.gov.health.platform.devicevigilance;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Medical-device vigilance (module 168). Malfunction and harm
 *  reports are received and risk-assessed; regulatory reporting
 *  duties are enforced for serious incidents; field corrections
 *  and recalls are managed; the device, its accessories and its
 *  location are tracked; and every incident stays linked to
 *  maintenance, training and its manufacturing lot. */
@RestController @RequestMapping("/api/device-vigilance")
public class DeviceVigilanceController {
  private final DeviceIncidentReportRepository reports; private final VigilanceAssessmentRepository assessments;
  private final FieldSafetyActionRepository actions; private final DeviceTrackingRepository tracking; private final AuditService audit;
  public DeviceVigilanceController(DeviceIncidentReportRepository reports, VigilanceAssessmentRepository assessments,
                                   FieldSafetyActionRepository actions, DeviceTrackingRepository tracking, AuditService audit) {
    this.reports=reports; this.assessments=assessments; this.actions=actions; this.tracking=tracking; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private DeviceIncidentReport report(UUID id) {
    return reports.findById(id).orElseThrow(() -> BizException.notFound("report_not_found","البلاغ غير موجود"));
  }

  public record ReportRequest(String deviceRef, String serialNo, String lotNo, String incidentType, String severity,
                              String description, String linkedMaintenanceRef, Boolean trainingNeed) {}
  @PostMapping("/reports")
  Map<String,Object> file(@RequestBody ReportRequest r) {
    var p = principal();
    var rep = reports.save(new DeviceIncidentReport(UUID.fromString(p.facilityId()), p.governorate(), r.deviceRef(), r.serialNo(), r.lotNo(),
      r.incidentType(), r.severity(), r.description(), p.email(), r.linkedMaintenanceRef(), Boolean.TRUE.equals(r.trainingNeed())));
    tracking.findBySerialNo(r.serialNo()).orElseGet(() ->
      tracking.save(new DeviceTracking(UUID.fromString(p.facilityId()), p.governorate(), r.deviceRef(), r.serialNo(), "clinical_area")));
    audit.record("vigilance.report.filed","device_incident_report",rep.getId().toString(),"{}");
    return Map.of("ok",true,"report",rep);
  }
  public record AssessRequest(String riskLevel) {}
  @PostMapping("/reports/{id}/assess")
  Map<String,Object> assess(@PathVariable UUID id, @RequestBody AssessRequest r) {
    var p = principal(); var rep = report(id);
    boolean required = VigilanceRules.regulatoryReportRequired(rep.getSeverity(), rep.getIncidentType());
    var a = assessments.save(new VigilanceAssessment(UUID.fromString(p.facilityId()), p.governorate(), id, r.riskLevel(), required));
    rep.setStatus("assessed"); reports.save(rep);
    audit.record("vigilance.assessed","vigilance_assessment",a.getId().toString(),"{}");
    return Map.of("ok",true,"assessment",a,"regulatory_report_required",required);
  }
  @PostMapping("/reports/{id}/regulatory-report-sent")
  Map<String,Object> regulatorySent(@PathVariable UUID id) {
    principal(); report(id);
    var a = assessments.findByReportId(id).orElseThrow(() -> BizException.notFound("assessment_not_found","التقييم غير موجود"));
    a.markReported(); assessments.save(a);
    audit.record("vigilance.regulatory.sent","vigilance_assessment",a.getId().toString(),"{}");
    return Map.of("ok",true,"assessment",a);
  }
  public record ActionRequest(String actionType, String scope) {}
  @PostMapping("/reports/{id}/field-actions")
  Map<String,Object> action(@PathVariable UUID id, @RequestBody ActionRequest r) {
    var p = principal(); var rep = report(id);
    var a = actions.save(new FieldSafetyAction(UUID.fromString(p.facilityId()), p.governorate(), id, r.actionType(), r.scope()));
    tracking.findBySerialNo(rep.getSerialNo()).ifPresent(t -> { t.setStatus(VigilanceRules.trackingStatusFor(r.actionType())); tracking.save(t); });
    rep.setStatus("action_taken"); reports.save(rep);
    audit.record("vigilance.field_action."+r.actionType(),"field_safety_action",a.getId().toString(),"{}");
    return Map.of("ok",true,"action",a,"device_tracking_status",VigilanceRules.trackingStatusFor(r.actionType()));
  }
  @PostMapping("/reports/{id}/close")
  Map<String,Object> close(@PathVariable UUID id) {
    principal(); var rep = report(id);
    var a = assessments.findByReportId(id).orElse(null);
    boolean required = a != null && a.isRegulatoryReportRequired();
    boolean reported = a != null && a.isRegulatoryReported();
    var acts = actions.findByReportId(id);
    String actionStatus = acts.isEmpty() ? "none" : acts.stream().allMatch(x -> "completed".equals(x.getStatus())) ? "completed" : "open";
    if (!VigilanceRules.caseClosable(required, reported, actionStatus))
      throw BizException.conflict("closure_blocked","لا إغلاق قبل إرسال البلاغ التنظيمي الواجب واكتمال إجراءات السلامة الميدانية");
    rep.setStatus("closed"); reports.save(rep);
    return Map.of("ok",true,"report",rep);
  }
}
