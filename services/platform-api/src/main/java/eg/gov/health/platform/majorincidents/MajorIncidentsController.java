package eg.gov.health.platform.majorincidents;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.*; import java.util.*;
/** Major incidents & community readiness (module 76).
 *  Multi-agency response plans with command chains and
 *  alternative communications; reserve and volunteer rosters;
 *  medical shelter centres and field hospitals; training,
 *  simulation and lessons-learned records; public awareness
 *  messaging from verified sources with rumour control; and
 *  community psychological support after mass events. */
@RestController @RequestMapping("/api/major-incidents")
public class MajorIncidentsController {
  private final MajorIncidentPlanRepository plans; private final ReserveRosterEntryRepository roster;
  private final ReadinessExerciseRepository exercises; private final PublicHealthMessageRepository messages; private final AuditService audit;
  public MajorIncidentsController(MajorIncidentPlanRepository plans, ReserveRosterEntryRepository roster,
                                  ReadinessExerciseRepository exercises, PublicHealthMessageRepository messages, AuditService audit) {
    this.plans=plans; this.roster=roster; this.exercises=exercises; this.messages=messages; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private MajorIncidentPlan plan(UUID id) {
    return plans.findById(id).orElseThrow(() -> BizException.notFound("plan_not_found","الخطة غير موجودة"));
  }

  public record PlanRequest(String name, String agenciesInvolved, String commandChain, String alternativeComms) {}
  @PostMapping("/plans")
  Map<String,Object> create(@RequestBody PlanRequest r) {
    var p = principal();
    var pl = plans.save(new MajorIncidentPlan(UUID.fromString(p.facilityId()), p.governorate(), r.name(), r.agenciesInvolved(), r.commandChain(), r.alternativeComms()));
    return Map.of("ok",true,"plan",pl);
  }
  public record ExerciseRequest(String exerciseType, LocalDate conductedAt, Boolean lessonsRecorded) {}
  @PostMapping("/plans/{id}/exercises")
  Map<String,Object> exercise(@PathVariable UUID id, @RequestBody ExerciseRequest r) {
    var p = principal(); plan(id);
    var e = exercises.save(new ReadinessExercise(UUID.fromString(p.facilityId()), p.governorate(), id, r.exerciseType(), r.conductedAt(), Boolean.TRUE.equals(r.lessonsRecorded())));
    audit.record("readiness.exercise.conducted","readiness_exercise",e.getId().toString(),"{}");
    return Map.of("ok",true,"exercise",e);
  }
  @PostMapping("/plans/{id}/activate")
  Map<String,Object> activate(@PathVariable UUID id) {
    principal(); var pl = plan(id);
    var last = exercises.findFirstByPlanIdOrderByConductedAtDesc(id).map(ReadinessExercise::getConductedAt).orElse(null);
    if (!ReadinessRules.activationReady(pl.getCommandChain(), pl.getAlternativeComms(), last, LocalDate.now()))
      throw BizException.conflict("not_ready","التفعيل لخطة بسلسلة قيادة واتصال بديل وتدريب خلال 12 شهرًا — الجاهزية تذبل، وخطة بلا تدريب حديث ورقة");
    pl.setStatus("active"); plans.save(pl);
    audit.record("readiness.plan.activated","major_incident_plan",id.toString(),"{}");
    return Map.of("ok",true,"plan",pl);
  }
  public record RosterRequest(String memberRef, String memberType, String role, Boolean verified) {}
  @PostMapping("/plans/{id}/roster")
  Map<String,Object> rosterAdd(@PathVariable UUID id, @RequestBody RosterRequest r) {
    var p = principal(); plan(id);
    var m = roster.save(new ReserveRosterEntry(UUID.fromString(p.facilityId()), p.governorate(), id, r.memberRef(), r.memberType(), r.role(), Boolean.TRUE.equals(r.verified())));
    return Map.of("ok",true,"member",m);
  }
  @PostMapping("/roster/{mid}/deploy")
  Map<String,Object> deploy(@PathVariable UUID mid) {
    principal();
    var m = roster.findById(mid).orElseThrow(() -> BizException.notFound("member_not_found","العضو غير موجود"));
    if (!ReadinessRules.deploymentPermitted(m.isVerified()))
      throw BizException.conflict("unverified_member","النشر للأعضاء الموثقين فقط — المتطوع غير الموثق عبء في الميدان لا عون");
    m.deploy(); roster.save(m);
    return Map.of("ok",true,"member",m);
  }
  public record MessageRequest(UUID planId, String message, Boolean sourceVerified, Boolean rumorRefutation) {}
  @PostMapping("/messages")
  Map<String,Object> message(@RequestBody MessageRequest r) {
    var p = principal();
    var m = messages.save(new PublicHealthMessage(UUID.fromString(p.facilityId()), p.governorate(), r.planId(), r.message(),
      Boolean.TRUE.equals(r.sourceVerified()), Boolean.TRUE.equals(r.rumorRefutation())));
    return Map.of("ok",true,"message_item",m);
  }
  @PostMapping("/messages/{mid}/publish")
  Map<String,Object> publish(@PathVariable UUID mid) {
    principal();
    var m = messages.findById(mid).orElseThrow(() -> BizException.notFound("message_not_found","الرسالة غير موجودة"));
    if (!ReadinessRules.messagePublishable(m.isSourceVerified()))
      throw BizException.conflict("unverified_source","رسائل التوعية العامة من مصادر معتمدة موثقة فقط — رسالة غير موثقة في أزمة تمنح الشائعة صوتًا رسميًا");
    m.publish(); messages.save(m);
    audit.record("readiness.message.published","public_health_message",mid.toString(),"{}");
    return Map.of("ok",true,"message_item",m,"published",true);
  }
}
