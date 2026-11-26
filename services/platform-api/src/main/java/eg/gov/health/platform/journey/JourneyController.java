package eg.gov.health.platform.journey;
import eg.gov.health.platform.common.*;
import eg.gov.health.platform.notify.NotificationService;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;
/** Cross-module care journeys (completion batch 1). One
 *  journey threads the REAL records that operating modules
 *  create for a patient — encounter, lab orders, prescriptions,
 *  invoices, discharge — under a single correlation id, in
 *  canonical order, never backwards. Every step also appends
 *  a domain event to the outbox: the durable seam other
 *  modules poll today and a Kafka drain publishes tomorrow. */
@RestController @RequestMapping("/api/journeys")
public class JourneyController {
  private final CareJourneyRepository journeys; private final JourneyStepRepository steps; private final DomainEventRepository events; private final AuditService audit; private final NotificationService notify;
  public JourneyController(CareJourneyRepository journeys, JourneyStepRepository steps, DomainEventRepository events, AuditService audit, NotificationService notify) {
    this.journeys=journeys; this.steps=steps; this.events=events; this.audit=audit; this.notify=notify;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private CareJourney journey(UUID id) {
    return journeys.findById(id).orElseThrow(() -> BizException.notFound("journey_not_found","الرحلة غير موجودة"));
  }
  private Set<String> recordedKeys(UUID journeyId) {
    return steps.findByJourneyIdOrderByCreatedAtAsc(journeyId).stream().map(JourneyStep::getStepKey).collect(Collectors.toSet());
  }

  public record StartBody(UUID patientId, UUID encounterId) {}
  @PostMapping
  Map<String,Object> start(@RequestBody StartBody r) {
    var p = principal();
    if (r.patientId() == null) throw BizException.badRequest("patient_required","رحلة بلا مريض ليست رحلة");
    String correlation = "JRN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    var j = journeys.save(new CareJourney(UUID.fromString(p.facilityId()), p.governorate(), r.patientId(), r.encounterId(), correlation));
    events.save(new DomainEvent(UUID.fromString(p.facilityId()), p.governorate(), "care_journey", j.getId(), "journey.started", correlation,
      "{\"patientId\":\"" + r.patientId() + "\"}"));
    return Map.of("ok",true,"journey",j,"correlation_id",correlation);
  }

  public record StepBody(String stepKey, String module, String entityType, UUID entityId, String detail) {}
  @PostMapping("/{id}/steps")
  Map<String,Object> step(@PathVariable UUID id, @RequestBody StepBody r) {
    var p = principal(); var j = journey(id);
    if ("completed".equals(j.getStatus())) throw BizException.conflict("journey_closed","رحلة مكتملة لا تقبل خطوات جديدة");
    if (JourneyRules.indexOf(r.stepKey()) < 0) throw BizException.badRequest("unknown_step","مرحلة غير معروفة: " + r.stepKey());
    if (r.entityId() != null && steps.existsByJourneyIdAndEntityId(id, r.entityId()))
      throw BizException.conflict("step_duplicate","هذا السجل مربوط بالرحلة من قبل — الربط لا يتكرر لا يتكرر");
    var recorded = recordedKeys(id);
    if (!JourneyRules.canRecord(recorded, r.stepKey()))
      throw BizException.conflict("step_backwards","لا خطوات للخلف: المرحلة " + r.stepKey() + " تسبق مراحل مسجلة — " + recorded);
    var s = steps.save(new JourneyStep(UUID.fromString(p.facilityId()), p.governorate(), id, r.stepKey(), r.module(), r.entityType(), r.entityId(), r.detail()));
    events.save(new DomainEvent(UUID.fromString(p.facilityId()), p.governorate(), "journey_step", s.getId(), "journey.step." + r.stepKey(), j.getCorrelationId(),
      "{\"entityType\":\"" + r.entityType() + "\",\"entityId\":\"" + r.entityId() + "\"}"));
    audit.record("journey.step.recorded","care_journey",id.toString(),"{}");
    return Map.of("ok",true,"step",s,"recorded_steps",recordedKeys(id));
  }

  @GetMapping("/{id}/timeline")
  Map<String,Object> timeline(@PathVariable UUID id) {
    principal(); var j = journey(id);
    var list = steps.findByJourneyIdOrderByCreatedAtAsc(id);
    return Map.of("ok",true,"journey",j,"steps",list,
      "missing_for_completion",JourneyRules.missingMandatory(recordedKeys(id)),
      "events",events.findByCorrelationIdOrderByCreatedAtAsc(j.getCorrelationId()));
  }

  @PostMapping("/{id}/complete")
  Map<String,Object> complete(@PathVariable UUID id) {
    principal(); var j = journey(id);
    var missing = JourneyRules.missingMandatory(recordedKeys(id));
    if (!missing.isEmpty())
      throw BizException.conflict("journey_incomplete","رحلة لا تكتمل بلا عمودها الفقري — النواقص: " + String.join("، ", missing));
    j.complete(); journeys.save(j);
    events.save(new DomainEvent(j.getId() == null ? null : UUID.fromString(TenantContext.get().facilityId()), TenantContext.get().governorate(),
      "care_journey", j.getId(), "journey.completed", j.getCorrelationId(), "{}"));
    notify.queue(UUID.fromString(TenantContext.get().facilityId()), TenantContext.get().governorate(), "facility-admin", "in_app",
      "اكتملت رحلة رعاية", "اكتملت الرحلة " + j.getCorrelationId() + " بعمودها الفقري كاملًا");
    audit.record("journey.completed","care_journey",id.toString(),"{}");
    return Map.of("ok",true,"journey",j,"status","completed");
  }
}
