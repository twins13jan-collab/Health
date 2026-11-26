package eg.gov.health.platform.followup;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.*; import java.util.*;
/** Post-discharge follow-up & continuity of care (module 184).
 *  Discharge creates a follow-up plan with a named owner and dated
 *  tasks: calls that confirm the patient understood the instructions,
 *  reviews of results that were still pending at discharge, and
 *  checks on high-risk medications. Overdue tasks escalate; a
 *  patient unreachable after three documented attempts escalates
 *  too. An abnormal late result is reviewed and the patient
 *  notified — both documented. Referrals hand continuity to primary
 *  or home care against acceptance. A plan closes only with nothing
 *  escalated, unreachable or unreviewed left open. */
@RestController @RequestMapping("/api/follow-up")
public class FollowUpController {
  private final FollowUpPlanRepository plans; private final FollowUpTaskRepository tasks;
  private final PendingResultReviewRepository results; private final HighRiskMedicationCheckRepository medChecks;
  private final ContinuityReferralRepository referrals; private final AuditService audit;
  public FollowUpController(FollowUpPlanRepository plans, FollowUpTaskRepository tasks, PendingResultReviewRepository results,
                            HighRiskMedicationCheckRepository medChecks, ContinuityReferralRepository referrals, AuditService audit) {
    this.plans=plans; this.tasks=tasks; this.results=results; this.medChecks=medChecks; this.referrals=referrals; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private FollowUpPlan plan(UUID id) {
    return plans.findById(id).orElseThrow(() -> BizException.notFound("plan_not_found","خطة المتابعة غير موجودة"));
  }

  public record PlanRequest(String patientRef, String dischargeRef, String responsible, String planSummary) {}
  @PostMapping("/plans")
  Map<String,Object> createPlan(@RequestBody PlanRequest r) {
    var p = principal();
    if (r.responsible()==null||r.responsible().isBlank())
      throw BizException.badRequest("responsible_required","خطة المتابعة عند الخروج تسمي مسؤولًا عنها");
    var pl = plans.save(new FollowUpPlan(UUID.fromString(p.facilityId()), p.governorate(), r.patientRef(), r.dischargeRef(), r.responsible(), r.planSummary()));
    audit.record("followup.plan.created","followup_plan",pl.getId().toString(),"{}");
    return Map.of("ok",true,"plan",pl);
  }
  public record TaskRequest(String kind, OffsetDateTime dueAt) {}
  @PostMapping("/plans/{id}/tasks")
  Map<String,Object> task(@PathVariable UUID id, @RequestBody TaskRequest r) {
    var p = principal(); var pl = plan(id);
    var t = tasks.save(new FollowUpTask(pl.getFacilityId(), p.governorate(), id, r.kind(), r.dueAt()));
    audit.record("followup.task.created","followup_task",t.getId().toString(),"{}");
    return Map.of("ok",true,"task",t);
  }
  @PostMapping("/tasks/{id}/complete")
  Map<String,Object> completeTask(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var t = tasks.findById(id).orElseThrow(() -> BizException.notFound("task_not_found","المهمة غير موجودة"));
    t.complete(body.get("outcome")); tasks.save(t);
    audit.record("followup.task.done","followup_task",id.toString(),"{}");
    return Map.of("ok",true,"task",t);
  }
  @PostMapping("/tasks/{id}/attempt")
  Map<String,Object> attempt(@PathVariable UUID id) {
    var t = tasks.findById(id).orElseThrow(() -> BizException.notFound("task_not_found","المهمة غير موجودة"));
    t.attempt();
    String state = FollowUpRules.taskState(t.getStatus(), t.getDueAt(), OffsetDateTime.now(), t.getContactAttempts());
    if (!state.equals(t.getStatus())) { t.setStatus(state); }
    tasks.save(t);
    audit.record("followup.task.attempt","followup_task",id.toString(),"{}");
    return Map.of("ok",true,"task",t,"state",state,"escalated","unreachable".equals(state) || "escalated".equals(state));
  }
  @PostMapping("/tasks/{id}/evaluate")
  Map<String,Object> evaluate(@PathVariable UUID id) {
    var t = tasks.findById(id).orElseThrow(() -> BizException.notFound("task_not_found","المهمة غير موجودة"));
    String state = FollowUpRules.taskState(t.getStatus(), t.getDueAt(), OffsetDateTime.now(), t.getContactAttempts());
    if (!state.equals(t.getStatus())) { t.setStatus(state); tasks.save(t); }
    audit.record("followup.task.evaluated","followup_task",id.toString(),"{}");
    return Map.of("ok",true,"task",t,"state",state);
  }
  public record ResultRequest(String testName, Boolean abnormal) {}
  @PostMapping("/plans/{id}/pending-results")
  Map<String,Object> pendingResult(@PathVariable UUID id, @RequestBody ResultRequest r) {
    var p = principal(); var pl = plan(id);
    var rr = results.save(new PendingResultReview(pl.getFacilityId(), p.governorate(), id, r.testName(), Boolean.TRUE.equals(r.abnormal())));
    audit.record("followup.result.arrived","pending_result_review",rr.getId().toString(),"{}");
    return Map.of("ok",true,"pending_result",rr,"note","results pending at discharge are tracked until reviewed and the patient notified");
  }
  public record ReviewRequest(String actionTaken, Boolean patientNotified) {}
  @PostMapping("/pending-results/{id}/review")
  Map<String,Object> reviewResult(@PathVariable UUID id, @RequestBody ReviewRequest r) {
    var p = principal();
    var rr = results.findById(id).orElseThrow(() -> BizException.notFound("result_not_found","النتيجة غير موجودة"));
    rr.review(p.email(), r.actionTaken(), Boolean.TRUE.equals(r.patientNotified())); results.save(rr);
    boolean breach = FollowUpRules.abnormalNotificationBreach(rr.isAbnormal(), rr.getReviewedBy()!=null, rr.isPatientNotified());
    audit.record("followup.result.reviewed","pending_result_review",id.toString(),"{}");
    return Map.of("ok",true,"pending_result",rr,"notification_breach",breach,
      "note", breach ? "abnormal result reviewed but the patient was NOT notified — open safety item" : "review complete");
  }
  public record MedCheckRequest(String medication, String checkType, String finding, String action) {}
  @PostMapping("/plans/{id}/medication-checks")
  Map<String,Object> medCheck(@PathVariable UUID id, @RequestBody MedCheckRequest r) {
    var p = principal(); var pl = plan(id);
    var mc = medChecks.save(new HighRiskMedicationCheck(pl.getFacilityId(), p.governorate(), id, r.medication(), r.checkType(), r.finding(), r.action(), p.email()));
    audit.record("followup.medication.checked","high_risk_medication_check",mc.getId().toString(),"{}");
    return Map.of("ok",true,"medication_check",mc);
  }
  public record ReferralRequest(String target, String referralRef) {}
  @PostMapping("/plans/{id}/referrals")
  Map<String,Object> referral(@PathVariable UUID id, @RequestBody ReferralRequest r) {
    var p = principal(); var pl = plan(id);
    var ref = referrals.save(new ContinuityReferral(pl.getFacilityId(), p.governorate(), id, r.target(), r.referralRef()));
    audit.record("followup.referral.sent","continuity_referral",ref.getId().toString(),"{}");
    return Map.of("ok",true,"referral",ref,"note","continuity continues only against acceptance by the receiving service");
  }
  @PostMapping("/referrals/{id}/accept")
  Map<String,Object> acceptReferral(@PathVariable UUID id) {
    var p = principal();
    var ref = referrals.findById(id).orElseThrow(() -> BizException.notFound("referral_not_found","الإحالة غير موجودة"));
    ref.accept(p.email()); referrals.save(ref);
    audit.record("followup.referral.accepted","continuity_referral",id.toString(),"{}");
    return Map.of("ok",true,"referral",ref);
  }
  @PostMapping("/plans/{id}/close")
  Map<String,Object> close(@PathVariable UUID id) {
    var pl = plan(id);
    var planTasks = tasks.findByPlanId(id);
    long escalated = planTasks.stream().filter(t -> "escalated".equals(t.getStatus()) || "missed".equals(t.getStatus())).count();
    long unreachable = planTasks.stream().filter(t -> "unreachable".equals(t.getStatus())).count();
    var planResults = results.findByPlanId(id);
    long unreviewed = planResults.stream().filter(rr -> rr.getReviewedBy()==null
      || FollowUpRules.abnormalNotificationBreach(rr.isAbnormal(), true, rr.isPatientNotified())).count();
    if (!FollowUpRules.closable((int) escalated, (int) unreviewed, (int) unreachable))
      throw BizException.conflict("open_items","خطة المتابعة لا تغلق ومهام مصعدة أو مريض يتعذر الوصول إليه أو نتائج غير مراجعة ما زالت مفتوحة");
    pl.setStatus("closed"); plans.save(pl);
    audit.record("followup.plan.closed","followup_plan",id.toString(),"{}");
    return Map.of("ok",true,"plan",pl);
  }
}
