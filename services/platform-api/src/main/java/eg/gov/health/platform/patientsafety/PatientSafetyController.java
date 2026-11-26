package eg.gov.health.platform.patientsafety;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Patient safety & learning from incidents (module 156).
 *  Incidents and near misses are classified; reporting is
 *  confidential and reporters protected; the patient is
 *  notified per policy and law; systemic and human causes are
 *  analyzed; recurrence-prevention plans are managed; and the
 *  effect of safety actions is measured. */
@RestController @RequestMapping("/api/patient-safety")
public class PatientSafetyController {
  private final SafetyIncidentRepository incidents; private final IncidentAnalysisRepository analyses;
  private final PreventionActionRepository actions; private final AuditService audit;
  public PatientSafetyController(SafetyIncidentRepository incidents, IncidentAnalysisRepository analyses,
                                 PreventionActionRepository actions, AuditService audit) {
    this.incidents=incidents; this.analyses=analyses; this.actions=actions; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private SafetyIncident incident(UUID id) {
    return incidents.findById(id).orElseThrow(() -> BizException.notFound("incident_not_found","الحادث غير موجود"));
  }

  public record IncidentBody(String incidentType, String severity, String reporterRef, String description) {}
  @PostMapping("/incidents")
  Map<String,Object> report(@RequestBody IncidentBody r) {
    var p = principal();
    var i = incidents.save(new SafetyIncident(UUID.fromString(p.facilityId()), p.governorate(), r.incidentType(), r.severity(), r.reporterRef(), r.description()));
    audit.record("safety.incident.reported","safety_incident",i.getId().toString(),"{}");
    // Reporter identity is protected: it is never echoed back in responses.
    return Map.of("ok",true,"incident_id",i.getId().toString(),"incident_type",i.getIncidentType(),
      "reporter_protected",true,"note","the report is recorded; who reported it is not disclosed — an identity exposed is a silence purchased");
  }
  @PostMapping("/incidents/{id}/notify-patient")
  Map<String,Object> notify(@PathVariable UUID id) {
    principal(); var i = incident(id);
    i.notifyPatient(); incidents.save(i);
    return Map.of("ok",true,"incident_id",id.toString(),"patient_notified",true);
  }
  public record AnalysisBody(String systemicCauses, String humanFactors) {}
  @PostMapping("/incidents/{id}/analyses")
  Map<String,Object> analyze(@PathVariable UUID id, @RequestBody AnalysisBody r) {
    var p = principal(); var i = incident(id);
    var a = analyses.save(new IncidentAnalysis(UUID.fromString(p.facilityId()), p.governorate(), id, r.systemicCauses(), r.humanFactors()));
    i.markAnalyzed(); incidents.save(i);
    return Map.of("ok",true,"analysis",a);
  }
  public record ActionBody(Boolean done) {}
  @PostMapping("/analyses/{analysisId}/actions")
  Map<String,Object> action(@PathVariable UUID analysisId, @RequestBody ActionBody r) {
    var p = principal();
    var a = actions.save(new PreventionAction(UUID.fromString(p.facilityId()), p.governorate(), analysisId, "إجراء منع تكرار", Boolean.TRUE.equals(r.done())));
    return Map.of("ok",true,"action",a);
  }
  @PostMapping("/incidents/{id}/close")
  Map<String,Object> close(@PathVariable UUID id) {
    principal(); var i = incident(id);
    var ans = analyses.findByIncidentId(id);
    boolean analyzed = !ans.isEmpty();
    boolean allDone = analyzed && ans.stream().allMatch(a -> {
      var acts = actions.findByAnalysisId(a.getId());
      return !acts.isEmpty() && acts.stream().allMatch(PreventionAction::isDone);
    });
    if (!PatientSafetyRules.closable(i.getIncidentType(), i.isPatientNotified(), analyzed, allDone))
      throw BizException.conflict("not_closable","حادث سلامة لا يُغلق قبل إخطار المريض المتضرر وتحليل نظامي واكتمال كل إجراءات منع التكرار");
    i.close(); incidents.save(i);
    audit.record("safety.incident.closed","safety_incident",id.toString(),"{}");
    return Map.of("ok",true,"incident_id",id.toString(),"status","closed");
  }
}
