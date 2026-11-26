package eg.gov.health.platform.cycles;
import eg.gov.health.platform.common.*;
import jakarta.transaction.Transactional;
import org.springframework.web.bind.annotation.*;
import java.time.OffsetDateTime; import java.util.*;
@RestController @RequestMapping("/api/cycle-engine")
public class CycleController {
  private final CycleDefinitionRepository defs; private final CycleInstanceRepository instances;
  private final CycleEventRepository events; private final AuditService audit;
  public CycleController(CycleDefinitionRepository defs, CycleInstanceRepository instances, CycleEventRepository events, AuditService audit) {
    this.defs = defs; this.instances = instances; this.events = events; this.audit = audit;
  }
  private UUID facility() {
    var p = TenantContext.get();
    if (p == null || p.facilityId() == null) throw BizException.forbidden("يتطلب حساب منشأة لتشغيل دورات العمل");
    return UUID.fromString(p.facilityId());
  }
  @GetMapping("/definitions")
  Map<String, Object> definitions() {
    var rows = defs.findByStatusOrderByCatalogNumber("active");
    return Map.of("ok", true, "definitions", rows, "count", rows.size());
  }
  @GetMapping("/transitions")
  Map<String, Object> transitions() {
    return Map.of("ok", true, "transitions", CycleStateMachine.TRANSITIONS, "statuses", CycleStateMachine.STATUSES,
      "terminal", CycleStateMachine.TERMINAL, "reason_required", CycleStateMachine.REASON_REQUIRED);
  }
  public record CreateInstance(String definitionCode, String title, String subjectType, String subjectRef,
                               UUID patientId, String protocolVersion, String correlationId, String idempotencyKey, Integer slaHours) {}
  @PostMapping("/instances") @Transactional
  Map<String, Object> create(@RequestBody CreateInstance r) {
    UUID fac = facility(); var p = TenantContext.get();
    if (r.definitionCode() == null || r.title() == null) throw BizException.badRequest("missing_field", "definitionCode و title مطلوبان");
    var def = defs.findByCode(r.definitionCode()).orElseThrow(() -> BizException.badRequest("unknown_definition", "تعريف دورة غير معروف"));
    if (r.idempotencyKey() != null) {
      var dup = instances.findByFacilityIdAndIdempotencyKey(fac, r.idempotencyKey());
      if (dup.isPresent()) return Map.of("ok", true, "replayed", true, "instance", dup.get());
    }
    int sla = r.slaHours() != null ? r.slaHours() : def.getSlaHours();
    var inst = instances.save(new CycleInstance(fac, p.governorate(), r.definitionCode(), r.subjectType(), r.subjectRef(),
      r.patientId(), r.title(), r.protocolVersion(), r.correlationId() != null ? r.correlationId() : UUID.randomUUID().toString(),
      r.idempotencyKey(), OffsetDateTime.now().plusHours(sla)));
    events.save(new CycleEvent(fac, p.governorate(), inst.getId(), "created", null, "draft", p.userId(), p.role(), null));
    audit.record("cycle.created", "cycle_instance", inst.getId().toString(), "{}");
    return Map.of("ok", true, "instance", inst);
  }
  @GetMapping("/instances")
  Map<String, Object> list() {
    var rows = instances.findByFacilityIdOrderByCreatedAtDesc(facility());
    return Map.of("ok", true, "instances", rows, "count", rows.size());
  }
  @GetMapping("/instances/{id}")
  CycleInstance get(@PathVariable UUID id) {
    var inst = instances.findById(id).orElseThrow(() -> BizException.notFound("instance_not_found", "الدورة غير موجودة"));
    if (!inst.getFacilityId().equals(facility())) throw BizException.notFound("instance_not_found", "الدورة غير موجودة");
    return inst;
  }
  @GetMapping("/instances/{id}/events")
  Map<String, Object> instanceEvents(@PathVariable UUID id) {
    get(id); var rows = events.findByInstanceIdOrderByCreatedAtAsc(id);
    return Map.of("ok", true, "events", rows, "count", rows.size());
  }
  public record TransitionRequest(String toStatus, String reason, String currentStep) {}
  @PostMapping("/instances/{id}/transition") @Transactional
  Map<String, Object> transition(@PathVariable UUID id, @RequestBody TransitionRequest r) {
    var p = TenantContext.get(); var inst = get(id);
    String to = r.toStatus();
    if (to == null || !CycleStateMachine.STATUSES.contains(to)) throw BizException.badRequest("invalid_status", "حالة غير معروفة");
    if (CycleStateMachine.REASON_REQUIRED.contains(to) && (r.reason() == null || r.reason().isBlank()))
      throw BizException.badRequest("reason_required", "هذا الانتقال يتطلب توثيق السبب (كتالوج v6)");
    if (!CycleStateMachine.isLegal(inst.getStatus(), to))
      throw BizException.conflict("illegal_transition", "انتقال غير مسموح: " + inst.getStatus() + " -> " + to
        + " | المسموح: " + CycleStateMachine.TRANSITIONS.getOrDefault(inst.getStatus(), List.of()));
    String from = inst.getStatus();
    inst.transitionTo(to, r.currentStep()); instances.save(inst);
    events.save(new CycleEvent(inst.getFacilityId(), p == null ? null : p.governorate(), id, "transition", from, to,
      p == null ? null : p.userId(), p == null ? null : p.role(), r.reason()));
    audit.record("cycle.transition." + to, "cycle_instance", id.toString(), "{}");
    return Map.of("ok", true, "instance", inst);
  }
  @PostMapping("/instances/{id}/escalate") @Transactional
  Map<String, Object> escalate(@PathVariable UUID id, @RequestBody Map<String, String> body) {
    var p = TenantContext.get(); var inst = get(id);
    if (body.getOrDefault("reason", "").isBlank()) throw BizException.badRequest("reason_required", "سبب التصعيد مطلوب");
    if (CycleStateMachine.TERMINAL.contains(inst.getStatus())) throw BizException.conflict("terminal_instance", "دورة منتهية لا تُصعَّد");
    inst.escalate(); instances.save(inst);
    events.save(new CycleEvent(inst.getFacilityId(), p == null ? null : p.governorate(), id, "escalated", inst.getStatus(), inst.getStatus(),
      p == null ? null : p.userId(), p == null ? null : p.role(), body.get("reason")));
    return Map.of("ok", true, "instance", inst);
  }
  @GetMapping("/summary")
  Map<String, Object> summary() {
    UUID fac = facility(); var byStatus = new java.util.LinkedHashMap<String, Long>();
    for (String s : CycleStateMachine.STATUSES) byStatus.put(s, instances.countByFacilityIdAndStatus(fac, s));
    var breaches = instances.findByFacilityIdAndSlaDueAtBeforeAndStatusNotIn(fac, OffsetDateTime.now(), List.of("closed", "cancelled", "completed"));
    return Map.of("ok", true, "summary", Map.of("by_status", byStatus, "sla_breaches", breaches.size()));
  }
  @GetMapping("/sla-breaches")
  Map<String, Object> slaBreaches() {
    var rows = instances.findByFacilityIdAndSlaDueAtBeforeAndStatusNotIn(facility(), OffsetDateTime.now(), List.of("closed", "cancelled", "completed"));
    return Map.of("ok", true, "sla_breaches", rows, "count", rows.size());
  }
}
