package eg.gov.health.platform.integrations;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Integrations & message monitoring (cycle 234). Endpoints are
 *  versioned and compatibility-tested before activation; contracts
 *  carry fields and validation rules; every message is traced by its
 *  idempotency key to an ACK or a final failure; invalid payloads are
 *  quarantined; retries are graded and exhaust into the dead-letter
 *  queue with a reconciliation case; operators reprocess — they never
 *  edit message content. */
@RestController @RequestMapping("/api/integrations")
public class IntegrationMonitoringController {
  private final IntegrationEndpointRepository endpoints; private final InterfaceContractRepository contracts;
  private final IntegrationMessageRepository messages; private final DeliveryAttemptRepository attempts;
  private final AcknowledgementRepository acks; private final MappingRuleRepository mappings;
  private final ReconciliationCaseRepository reconciliations; private final AuditService audit;
  public IntegrationMonitoringController(IntegrationEndpointRepository endpoints, InterfaceContractRepository contracts,
                               IntegrationMessageRepository messages, DeliveryAttemptRepository attempts,
                               AcknowledgementRepository acks, MappingRuleRepository mappings,
                               ReconciliationCaseRepository reconciliations, AuditService audit) {
    this.endpoints=endpoints; this.contracts=contracts; this.messages=messages; this.attempts=attempts;
    this.acks=acks; this.mappings=mappings; this.reconciliations=reconciliations; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private IntegrationEndpoint endpoint(UUID id) {
    return endpoints.findById(id).orElseThrow(() -> BizException.notFound("endpoint_not_found","نقطة الاتصال غير موجودة"));
  }
  private IntegrationMessage message(UUID id) {
    return messages.findById(id).orElseThrow(() -> BizException.notFound("message_not_found","الرسالة غير موجودة"));
  }

  public record EndpointRequest(String vendor, String systemName, String version, String protocol, String owner, String environment, Boolean compatibilityTested) {}
  @PostMapping("/endpoints")
  Map<String,Object> endpoint(@RequestBody EndpointRequest r) {
    var p = principal();
    if (r.systemName()==null||r.protocol()==null) throw BizException.badRequest("invalid_endpoint","النظام والبروتوكول مطلوبان");
    var e = endpoints.save(new IntegrationEndpoint(UUID.fromString(p.facilityId()), p.governorate(), r.vendor(),
      r.systemName(), r.version(), r.protocol(), r.owner(), r.environment(), Boolean.TRUE.equals(r.compatibilityTested())));
    audit.record("integration.endpoint.registered","integration_endpoint",e.getId().toString(),"{}");
    return Map.of("ok",true,"endpoint",e,"status","suspended until compatibility-tested and activated");
  }
  @PostMapping("/endpoints/{id}/compatibility-tested")
  Map<String,Object> tested(@PathVariable UUID id) {
    var e = endpoint(id); e.markTested(); endpoints.save(e);
    audit.record("integration.endpoint.tested","integration_endpoint",id.toString(),"{}");
    return Map.of("ok",true,"endpoint",e);
  }
  @PostMapping("/endpoints/{id}/activate")
  Map<String,Object> activate(@PathVariable UUID id) {
    var e = endpoint(id);
    if (!IntegrationRules.activatable(e.isCompatibilityTested()))
      throw BizException.conflict("compatibility_first","تغير إصدار المورد يتطلب اختبار توافق قبل التمرير/التفعيل");
    e.activate(); endpoints.save(e);
    audit.record("integration.endpoint.activated","integration_endpoint",id.toString(),"{}");
    return Map.of("ok",true,"endpoint",e);
  }
  @PostMapping("/endpoints/{id}/version")
  Map<String,Object> version(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var e = endpoint(id);
    if (body.get("version")==null) throw BizException.badRequest("version_required","الإصدار الجديد مطلوب");
    e.setVersion(body.get("version")); endpoints.save(e);
    audit.record("integration.endpoint.version_changed","integration_endpoint",id.toString(),"{}");
    return Map.of("ok",true,"endpoint",e,"note","version change suspends the endpoint until a new compatibility test passes");
  }
  public record ContractRequest(String messageType, String fields, String validationRules, Integer version) {}
  @PostMapping("/endpoints/{id}/contracts")
  Map<String,Object> contract(@PathVariable UUID id, @RequestBody ContractRequest r) {
    var p = principal(); var e = endpoint(id);
    var c = contracts.save(new InterfaceContract(e.getFacilityId(), p.governorate(), id, r.messageType(), r.fields(),
      r.validationRules(), r.version()==null?1:r.version()));
    audit.record("integration.contract.defined","interface_contract",c.getId().toString(),"{}");
    return Map.of("ok",true,"contract",c);
  }
  public record MappingRequest(String sourceField, String targetField, Integer version) {}
  @PostMapping("/endpoints/{id}/mappings")
  Map<String,Object> mapping(@PathVariable UUID id, @RequestBody MappingRequest r) {
    var p = principal(); var e = endpoint(id);
    var m = mappings.save(new MappingRule(e.getFacilityId(), p.governorate(), id, r.sourceField(), r.targetField(),
      r.version()==null?1:r.version()));
    audit.record("integration.mapping.defined","mapping_rule",m.getId().toString(),"{}");
    return Map.of("ok",true,"mapping",m,"documented",true);
  }

  public record MessageRequest(String messageId, String sourceSystem, String destination, String contentHash, String traceId, Boolean payloadValid) {}
  @PostMapping("/messages")
  Map<String,Object> message(@RequestBody MessageRequest r) {
    var p = principal();
    if (r.messageId()==null) throw BizException.badRequest("message_id_required","معرف الرسالة (مفتاح عدم التكرار) مطلوب");
    // Idempotency: the same message id is never processed twice.
    var prior = messages.findByMessageId(r.messageId()).orElse(null);
    if (prior != null)
      return Map.of("ok",true,"duplicate",true,"message",prior,"note","duplicate blocked by idempotency key — not reprocessed");
    boolean valid = !Boolean.FALSE.equals(r.payloadValid());
    var m = messages.save(new IntegrationMessage(UUID.fromString(p.facilityId()), p.governorate(), r.messageId(),
      r.sourceSystem(), r.destination(), r.contentHash(), r.traceId(), valid));
    audit.record("integration.message.received","integration_message",m.getId().toString(),"{}");
    return Map.of("ok",true,"message",m,
      "note", valid ? "received — an HTTP 200 alone proves neither meaning nor effect; awaiting ACK"
                    : "invalid payload quarantined in the error queue");
  }
  public record AttemptRequest(String result, String errorCode) {}
  @PostMapping("/messages/{id}/attempts")
  Map<String,Object> attempt(@PathVariable UUID id, @RequestBody AttemptRequest r) {
    var p = principal(); var m = message(id);
    if ("quarantined".equals(m.getStatus()))
      throw BizException.conflict("quarantined","رسالة معزولة في قائمة الأخطاء لا تسلم قبل معالجة سبب العزل");
    int no = attempts.findByMessageRefOrderByAttemptNoAsc(id).size() + 1;
    var a = attempts.save(new DeliveryAttempt(mFacility(m), p.governorate(), id, no, r.result(), r.errorCode()));
    if ("failed".equals(r.result()) && IntegrationRules.deadLetter(no)) {
      m.setStatus("dead_letter"); messages.save(m);
      reconciliations.save(new ReconciliationCase(mFacility(m), p.governorate(), id,
        "delivery exhausted after " + no + " attempts", "pending operator reconciliation"));
    }
    audit.record("integration.delivery.attempt","delivery_attempt",a.getId().toString(),"{}");
    return Map.of("ok",true,"attempt",a,"message_status",m.getStatus(),
      "dead_letter", "dead_letter".equals(m.getStatus()));
  }
  private UUID mFacility(IntegrationMessage m) { return m.getFacilityId(); }
  public record AckRequest(String ackType, Boolean semanticVerified) {}
  @PostMapping("/messages/{id}/ack")
  Map<String,Object> ack(@PathVariable UUID id, @RequestBody AckRequest r) {
    var p = principal(); var m = message(id);
    if (r.ackType()==null) throw BizException.badRequest("ack_type_required","نوع الإقرار ACK/NACK مطلوب");
    boolean sem = Boolean.TRUE.equals(r.semanticVerified());
    var a = acks.save(new Acknowledgement(mFacility(m), p.governorate(), id, r.ackType(), sem));
    if (IntegrationRules.fullyAcknowledged(r.ackType(), sem)) {
      m.verifySemantics(); m.setStatus("acked"); messages.save(m);
    } else if ("ACK".equals(r.ackType())) {
      // Transport ACK without semantic verification: delivered, not proven.
      m.setStatus("received"); messages.save(m);
    } else {
      m.setStatus("nacked"); messages.save(m);
    }
    audit.record("integration.message.ack","acknowledgement",a.getId().toString(),"{}");
    return Map.of("ok",true,"acknowledgement",a,"message_status",m.getStatus(),
      "fully_acknowledged", IntegrationRules.fullyAcknowledged(r.ackType(), sem),
      "note","HTTP 200 is not proof of meaning or effect — only ACK + semantic verification completes a message");
  }
  /** The operator reprocesses — content is never edited. */
  @PostMapping("/messages/{id}/reprocess")
  Map<String,Object> reprocess(@PathVariable UUID id) {
    var m = message(id);
    if (!("nacked".equals(m.getStatus()) || "dead_letter".equals(m.getStatus())))
      throw BizException.conflict("not_reprocessable","إعادة المعالجة للرسائل المرفوضة أو الميتة فقط — والمحتوى لا يعدل أبدًا");
    m.setStatus("received"); messages.save(m);
    audit.record("integration.message.reprocessed","integration_message",id.toString(),"{}");
    return Map.of("ok",true,"message",m,"content_unchanged",true);
  }
  @PostMapping("/reconciliations/{id}/resolve")
  Map<String,Object> resolveRecon(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var c = reconciliations.findById(id).orElseThrow(() -> BizException.notFound("case_not_found","حالة المصالحة غير موجودة"));
    if (body.get("handling")==null||body.get("handling").isBlank())
      throw BizException.badRequest("handling_required","المصالحة تتطلب معالجة موثقة للفرق");
    c.resolve(body.get("handling")); reconciliations.save(c);
    audit.record("integration.reconciliation.resolved","reconciliation_case",id.toString(),"{}");
    return Map.of("ok",true,"case",c);
  }
  @GetMapping("/messages/quarantined")
  Map<String,Object> quarantined() {
    var rows = messages.findByStatusOrderByCreatedAtAsc("quarantined");
    return Map.of("ok",true,"messages",rows,"count",rows.size());
  }
}
