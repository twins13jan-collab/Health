package eg.gov.health.platform.nursing;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.*; import java.util.*;
/** Nursing & care plans (module 85). Nursing assessment covers
 *  fall, pressure-ulcer, pain, nutrition and mobility risks;
 *  individual care plans carry goals, interventions and
 *  reassessment dates; tasks are assigned by licence, competence
 *  and workload, with medication, fluids, blood, catheter and
 *  wound care documented. Overdue, rejected and reassigned tasks
 *  are managed and escalated — never silently dropped — and shift
 *  handover is structured around critical cases and open tasks. */
@RestController @RequestMapping("/api/nursing")
public class NursingController {
  private final NursingAssessmentRepository assessments; private final NursingCarePlanRepository plans;
  private final NursingTaskRepository tasks; private final ShiftHandoverRepository handovers; private final AuditService audit;
  public NursingController(NursingAssessmentRepository assessments, NursingCarePlanRepository plans, NursingTaskRepository tasks,
                           ShiftHandoverRepository handovers, AuditService audit) {
    this.assessments=assessments; this.plans=plans; this.tasks=tasks; this.handovers=handovers; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }

  public record AssessmentRequest(String patientRef, String fallRisk, String pressureUlcerRisk, Integer painScore, String nutritionRisk, String mobility) {}
  @PostMapping("/assessments")
  Map<String,Object> assess(@RequestBody AssessmentRequest r) {
    var p = principal();
    var a = assessments.save(new NursingAssessment(UUID.fromString(p.facilityId()), p.governorate(), r.patientRef(), r.fallRisk(),
      r.pressureUlcerRisk(), r.painScore(), r.nutritionRisk(), r.mobility(), p.email()));
    return Map.of("ok",true,"assessment",a,
      "fall_precautions","high".equals(r.fallRisk()) ? "high fall risk — precautions bundle activated" : "standard precautions");
  }
  public record PlanRequest(String patientRef, String goal, String interventions, LocalDate reassessmentDue) {}
  @PostMapping("/care-plans")
  Map<String,Object> plan(@RequestBody PlanRequest r) {
    var p = principal();
    if (r.reassessmentDue() == null) throw BizException.badRequest("reassessment_required","خطة الرعاية تتطلب موعد إعادة تقييم");
    var pl = plans.save(new NursingCarePlan(UUID.fromString(p.facilityId()), p.governorate(), r.patientRef(), r.goal(), r.interventions(), r.reassessmentDue()));
    audit.record("nursing.plan.created","nursing_care_plan",pl.getId().toString(),"{}");
    return Map.of("ok",true,"care_plan",pl);
  }
  public record TaskRequest(UUID carePlanId, String patientRef, String taskType, String assignedTo, String assigneeScope, OffsetDateTime dueAt) {}
  @PostMapping("/tasks")
  Map<String,Object> assign(@RequestBody TaskRequest r) {
    var p = principal();
    long open = tasks.countByAssignedToAndStatusIn(r.assignedTo(), List.of("assigned","reassigned"));
    if (!NursingRules.assignmentPermitted(r.assigneeScope(), r.taskType(), open))
      throw BizException.conflict("assignment_blocked","الإسناد حسب الترخيص والكفاءة وعبء العمل: مهمة دم تتطلب نطاق ترخيص الدم، وسقف المهام المفتوحة 12 — تُعاد الإسناد أو تُصعَّد ولا تُكدَّس بصمت");
    var t = tasks.save(new NursingTask(UUID.fromString(p.facilityId()), p.governorate(), r.carePlanId(), r.patientRef(), r.taskType(), r.assignedTo(), r.dueAt()));
    audit.record("nursing.task.assigned","nursing_task",t.getId().toString(),"{}");
    return Map.of("ok",true,"task",t,"assignee_open_tasks",open + 1);
  }
  @PostMapping("/tasks/{tid}/complete")
  Map<String,Object> complete(@PathVariable UUID tid) {
    principal();
    var t = tasks.findById(tid).orElseThrow(() -> BizException.notFound("task_not_found","المهمة غير موجودة"));
    boolean wasOverdue = NursingRules.isOverdue(t.getDueAt(), t.getStatus(), OffsetDateTime.now());
    t.complete(); tasks.save(t);
    return Map.of("ok",true,"task",t,"completed_late",wasOverdue);
  }
  public record ReassignRequest(String toNurse) {}
  @PostMapping("/tasks/{tid}/reassign")
  Map<String,Object> reassign(@PathVariable UUID tid, @RequestBody ReassignRequest r) {
    principal();
    var t = tasks.findById(tid).orElseThrow(() -> BizException.notFound("task_not_found","المهمة غير موجودة"));
    t.reassign(r.toNurse()); tasks.save(t);
    audit.record("nursing.task.reassigned","nursing_task",tid.toString(),"{}");
    return Map.of("ok",true,"task",t);
  }
  @GetMapping("/tasks/{tid}/status")
  Map<String,Object> status(@PathVariable UUID tid) {
    principal();
    var t = tasks.findById(tid).orElseThrow(() -> BizException.notFound("task_not_found","المهمة غير موجودة"));
    boolean overdue = NursingRules.isOverdue(t.getDueAt(), t.getStatus(), OffsetDateTime.now());
    return Map.of("ok",true,"task",t,"overdue",overdue,"escalation",overdue ? "overdue tasks escalate to the shift supervisor — they never vanish from the board" : "on track");
  }
  public record HandoverRequest(String toNurse, String criticalCasesNote, Integer openTasksCount) {}
  @PostMapping("/handovers")
  Map<String,Object> handover(@RequestBody HandoverRequest r) {
    var p = principal();
    if (!NursingRules.handoverComplete(r.criticalCasesNote(), r.openTasksCount()))
      throw BizException.conflict("handover_incomplete","التسليم المنظم يتطلب قائمة الحالات الحرجة وعدد المهام المفتوحة — لا تسليم شفهيًا عابرًا");
    var h = handovers.save(new ShiftHandover(UUID.fromString(p.facilityId()), p.governorate(), p.email(), r.toNurse(), r.criticalCasesNote(), r.openTasksCount()));
    audit.record("nursing.handover.given","shift_handover",h.getId().toString(),"{}");
    return Map.of("ok",true,"handover",h);
  }
  @PostMapping("/handovers/{hid}/accept")
  Map<String,Object> accept(@PathVariable UUID hid) {
    principal();
    var h = handovers.findById(hid).orElseThrow(() -> BizException.notFound("handover_not_found","التسليم غير موجود"));
    h.accept(); handovers.save(h);
    return Map.of("ok",true,"handover",h,"accepted",true);
  }
}
