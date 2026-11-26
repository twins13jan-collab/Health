package eg.gov.health.platform.endoscopy;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Endoscopy & interventional procedures (cycle 241). Every scope and
 *  reprocessing cycle binds to the patient and procedure; a scope that
 *  is unavailable, unfit, or not fully reprocessed cannot be booked; a
 *  failed test quarantines it with its affected cases traceable; no
 *  procedure starts on an incomplete checklist; unlabeled specimens are
 *  isolated, never attributed; discharge follows a recovery score. */
@RestController @RequestMapping("/api/endoscopy")
public class EndoscopyController {
  private final EndoscopyRequestRepository requests; private final PreProcedureChecklistRepository checklists;
  private final ScopeAssetRepository scopes; private final ReprocessingCycleRepository reprocessing;
  private final EndoscopyProcedureRepository procedures; private final SedationRecordRepository sedation;
  private final AuditService audit;
  public EndoscopyController(EndoscopyRequestRepository requests, PreProcedureChecklistRepository checklists,
                             ScopeAssetRepository scopes, ReprocessingCycleRepository reprocessing,
                             EndoscopyProcedureRepository procedures, SedationRecordRepository sedation, AuditService audit) {
    this.requests=requests; this.checklists=checklists; this.scopes=scopes; this.reprocessing=reprocessing;
    this.procedures=procedures; this.sedation=sedation; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private EndoscopyRequest request(UUID id) {
    return requests.findById(id).orElseThrow(() -> BizException.notFound("request_not_found","طلب المنظار غير موجود"));
  }
  private ScopeAsset scope(UUID id) {
    return scopes.findById(id).orElseThrow(() -> BizException.notFound("scope_not_found","المنظار غير موجود"));
  }
  private String latestReprocessingStatus(UUID scopeId) {
    var rows = reprocessing.findByScopeIdOrderByCompletedAtDesc(scopeId);
    return rows.isEmpty() ? "none" : rows.get(0).getStatus();
  }

  public record RequestDto(UUID patientId, String scopeType, String indication, String urgency, String preparationInstructions) {}
  @PostMapping("/requests")
  Map<String,Object> request(@RequestBody RequestDto r) {
    var p = principal();
    if (r.patientId()==null||r.scopeType()==null) throw BizException.badRequest("invalid_request","المريض ونوع المنظار مطلوبان");
    var req = requests.save(new EndoscopyRequest(UUID.fromString(p.facilityId()), p.governorate(), r.patientId(),
      r.scopeType(), r.indication(), r.urgency(), r.preparationInstructions()));
    audit.record("endoscopy.request.created","endoscopy_request",req.getId().toString(),"{}");
    return Map.of("ok",true,"request",req);
  }
  public record ChecklistDto(Boolean fastingConfirmed, Boolean medicationsReviewed, Boolean allergyChecked, Boolean consentDocumented, String sedationAssessment) {}
  @PostMapping("/requests/{id}/checklist")
  Map<String,Object> checklist(@PathVariable UUID id, @RequestBody ChecklistDto r) {
    var p = principal(); var req = request(id);
    var c = checklists.save(new PreProcedureChecklist(req.getFacilityId(), p.governorate(), id,
      Boolean.TRUE.equals(r.fastingConfirmed()), Boolean.TRUE.equals(r.medicationsReviewed()),
      Boolean.TRUE.equals(r.allergyChecked()), Boolean.TRUE.equals(r.consentDocumented()), r.sedationAssessment()));
    if (c.isCompleted()) { req.setStatus("prepared"); requests.save(req); }
    audit.record("endoscopy.checklist.recorded","pre_procedure_checklist",c.getId().toString(),"{}");
    return Map.of("ok",true,"checklist",c,"completed",c.isCompleted());
  }
  @PostMapping("/requests/{id}/postpone")
  Map<String,Object> postpone(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var req = request(id);
    if (body.get("reason")==null||body.get("reason").isBlank())
      throw BizException.badRequest("reason_required","تأجيل/إلغاء لعدم اكتمال التحضير يتطلب سببًا موثقًا وإشعار المريض");
    req.setStatus("postponed"); requests.save(req);
    audit.record("endoscopy.request.postponed","endoscopy_request",id.toString(),"{}");
    return Map.of("ok",true,"request",req,"patient_notified",true,"reason",body.get("reason"));
  }

  public record ScopeDto(String scopeCode, String scopeType) {}
  @PostMapping("/scopes")
  Map<String,Object> scope(@RequestBody ScopeDto r) {
    var p = principal();
    if (r.scopeCode()==null) throw BizException.badRequest("scope_code_required","معرف المنظار مطلوب");
    var s = scopes.save(new ScopeAsset(UUID.fromString(p.facilityId()), p.governorate(), r.scopeCode(), r.scopeType()));
    audit.record("endoscopy.scope.registered","scope_asset",s.getId().toString(),"{}");
    return Map.of("ok",true,"scope",s);
  }
  public record ReprocessingDto(Boolean cleaningDone, Boolean disinfectionDone, Boolean leakTestPassed, String operator) {}
  @PostMapping("/scopes/{id}/reprocessing")
  Map<String,Object> reprocess(@PathVariable UUID id, @RequestBody ReprocessingDto r) {
    var p = principal(); var s = scope(id);
    var cyc = reprocessing.save(new ReprocessingCycle(s.getFacilityId(), p.governorate(), id,
      Boolean.TRUE.equals(r.cleaningDone()), Boolean.TRUE.equals(r.disinfectionDone()),
      Boolean.TRUE.equals(r.leakTestPassed()), r.operator()));
    // A failed cycle/test quarantines the scope — use is blocked.
    if ("failed".equals(cyc.getStatus())) s.setStatus("quarantined");
    else if ("quarantined".equals(s.getStatus()) || "reprocessing".equals(s.getStatus())) s.setStatus("available");
    scopes.save(s);
    audit.record("endoscopy.scope.reprocessed","reprocessing_cycle",cyc.getId().toString(),"{}");
    return Map.of("ok",true,"cycle",cyc,"scope_status",s.getStatus());
  }
  @GetMapping("/scopes/{id}/affected-cases")
  Map<String,Object> affected(@PathVariable UUID id) {
    scope(id);
    var rows = procedures.findByScopeIdOrderByCreatedAtDesc(id);
    return Map.of("ok",true,"procedures_with_scope",rows,"count",rows.size(),
      "note","every procedure performed with this scope stays traceable for look-back");
  }

  public record StartDto(UUID scopeId, String physician) {}
  @PostMapping("/requests/{id}/start")
  Map<String,Object> start(@PathVariable UUID id, @RequestBody StartDto r) {
    var p = principal(); var req = request(id);
    var checklist = checklists.findByRequestIdOrderByCreatedAtDesc(id).stream().findFirst().orElse(null);
    if (checklist==null || !checklist.isCompleted())
      throw BizException.conflict("checklist_incomplete","لا بدء دون قائمة تحقق مكتملة: صيام وأدوية وحساسية وموافقة وتقييم تهدئة (دورة 241)");
    if (r.scopeId()==null) throw BizException.badRequest("scope_required","المنظار مطلوب");
    var s = scope(r.scopeId());
    String repro = latestReprocessingStatus(r.scopeId());
    if (!EndoscopyRules.scopeUsable(s.getStatus(), repro))
      throw BizException.conflict("scope_not_usable","منظار غير متاح أو غير صالح أو لم تكتمل إعادة معالجته — الحجز ممنوع (دورة 241)");
    var cycles = reprocessing.findByScopeIdOrderByCompletedAtDesc(r.scopeId());
    var proc = procedures.save(new EndoscopyProcedure(req.getFacilityId(), p.governorate(), id, r.scopeId(),
      cycles.isEmpty()?null:cycles.get(0).getId(), r.physician()));
    s.setStatus("in_use"); scopes.save(s);
    req.setStatus("in_procedure"); requests.save(req);
    audit.record("endoscopy.procedure.started","endoscopy_procedure",proc.getId().toString(),"{}");
    return Map.of("ok",true,"procedure",proc,"scope",s.getScopeCode());
  }

  public record CompleteDto(String sites, String findings, String interventions, String complications,
                            Boolean specimenSent, Boolean specimenLabelVerified, String followUpPlan, String dischargeInstructions) {}
  @PostMapping("/procedures/{id}/complete")
  Map<String,Object> complete(@PathVariable UUID id, @RequestBody CompleteDto r) {
    var proc = procedures.findById(id).orElseThrow(() -> BizException.notFound("procedure_not_found","الإجراء غير موجود"));
    boolean sent = Boolean.TRUE.equals(r.specimenSent());
    // A specimen without a trustworthy label is isolated — never attributed.
    if (sent && !Boolean.TRUE.equals(r.specimenLabelVerified()))
      throw BizException.badRequest("unlabeled_specimen","عينة بلا ملصق موثوق تُعزل ولا تُنسب لمريض (دورة 241)");
    if (r.followUpPlan()==null||r.followUpPlan().isBlank())
      throw BizException.badRequest("follow_up_required","إغلاق الإجراء يتطلب خطة متابعة وتعليمات خروج");
    proc.complete(r.sites(), r.findings(), r.interventions(), r.complications(), sent,
      Boolean.TRUE.equals(r.specimenLabelVerified()), r.followUpPlan(), r.dischargeInstructions());
    procedures.save(proc);
    var req = request(procRequestId(proc)); req.setStatus("completed"); requests.save(req);
    var s = scope(procScopeId(proc)); s.setStatus("reprocessing"); scopes.save(s);
    audit.record("endoscopy.procedure.completed","endoscopy_procedure",id.toString(),"{}");
    return Map.of("ok",true,"procedure",proc,
      "emergency_pathway", "complicated".equals(proc.getStatus()),
      "specimen_to_pathology", sent);
  }
  private UUID procRequestId(EndoscopyProcedure p) { return p.getRequestId(); }
  private UUID procScopeId(EndoscopyProcedure p) { return p.getScopeId(); }

  public record SedationDto(String drugs, String monitoring, Integer aldreteScore, String dischargeDecision) {}
  @PostMapping("/procedures/{id}/sedation")
  Map<String,Object> sedation(@PathVariable UUID id, @RequestBody SedationDto r) {
    var p = principal();
    var proc = procedures.findById(id).orElseThrow(() -> BizException.notFound("procedure_not_found","الإجراء غير موجود"));
    if (!EndoscopyRules.dischargeable(r.aldreteScore(), r.dischargeDecision()))
      throw BizException.badRequest("discharge_not_cleared","الخروج يتطلب تقييم إفاقة (Aldrete ≥ 9) أو قرار طبيب موثق");
    var rec = sedation.save(new SedationRecord(procFacility(proc), p.governorate(), id, r.drugs(), r.monitoring(), r.aldreteScore(), r.dischargeDecision()));
    audit.record("endoscopy.sedation.recorded","sedation_record",rec.getId().toString(),"{}");
    return Map.of("ok",true,"sedation",rec,"discharge_cleared",true);
  }
  private UUID procFacility(EndoscopyProcedure p) { return p.getFacilityId(); }

  @GetMapping("/requests")
  Map<String,Object> byPatient(@RequestParam UUID patientId) {
    var rows = requests.findByPatientIdOrderByCreatedAtDesc(patientId);
    return Map.of("ok",true,"requests",rows,"count",rows.size());
  }
}
