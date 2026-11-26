package eg.gov.health.platform.disasterops;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Multi-agency health disasters & crises (module 103). The
 *  national operations room and governorate and facility centres
 *  classify the event and activate plans on a decision log; a
 *  common situation picture covers casualties, beds, teams,
 *  stock and roads; resources are requested, allocated and
 *  delivered by logged decision; coordination follows the
 *  authorities of each agency. */
@RestController @RequestMapping("/api/disaster-ops")
public class DisasterOpsController {
  private final DisasterEventRepository events; private final SituationReportRepository situations;
  private final ResourceRequestRepository resources; private final DisasterDecisionRepository decisions; private final AuditService audit;
  public DisasterOpsController(DisasterEventRepository events, SituationReportRepository situations,
                               ResourceRequestRepository resources, DisasterDecisionRepository decisions, AuditService audit) {
    this.events=events; this.situations=situations; this.resources=resources; this.decisions=decisions; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private DisasterEvent event(UUID id) {
    return events.findById(id).orElseThrow(() -> BizException.notFound("event_not_found","الحدث غير موجود"));
  }

  public record EventRequest(String name, Integer classification) {}
  @PostMapping("/events")
  Map<String,Object> declare(@RequestBody EventRequest r) {
    var p = principal();
    var e = events.save(new DisasterEvent(UUID.fromString(p.facilityId()), p.governorate(), r.name(), r.classification()));
    audit.record("disaster.event.declared","disaster_event",e.getId().toString(),"{}");
    return Map.of("ok",true,"event",e);
  }
  @PostMapping("/events/{id}/activate-plan")
  Map<String,Object> activate(@PathVariable UUID id) {
    principal(); var e = event(id);
    if (!DisasterRules.activationPermitted(e.getClassification()))
      throw BizException.conflict("unclassified","تفعيل الخطط بعد تصنيف الحدث (1–3) — الاستجابة بحجم التصنيف لا بحجم الضجيج");
    e.activatePlan(); events.save(e);
    audit.record("disaster.plan.activated","disaster_event",id.toString(),"{}");
    return Map.of("ok",true,"event",e);
  }
  public record SituationRequest(Integer casualties, Integer bedsAvailable, Integer teamsDeployed, String stockNotes, String roadsNotes) {}
  @PostMapping("/events/{id}/situation")
  Map<String,Object> situation(@PathVariable UUID id, @RequestBody SituationRequest r) {
    var p = principal(); event(id);
    var s = situations.save(new SituationReport(UUID.fromString(p.facilityId()), p.governorate(), id, r.casualties(), r.bedsAvailable(), r.teamsDeployed(), r.stockNotes(), r.roadsNotes()));
    return Map.of("ok",true,"situation",s,"picture","casualties + beds + teams + stock + roads — one common operating picture");
  }
  public record ResourceBody(String resourceType, Integer quantity, String requestingFacility) {}
  @PostMapping("/events/{id}/resource-requests")
  Map<String,Object> request(@PathVariable UUID id, @RequestBody ResourceBody r) {
    var p = principal(); event(id);
    var rq = resources.save(new ResourceRequest(UUID.fromString(p.facilityId()), p.governorate(), id, r.resourceType(), r.quantity(), r.requestingFacility()));
    audit.record("disaster.resource.requested","resource_request",rq.getId().toString(),"{}");
    return Map.of("ok",true,"resource_request",rq);
  }
  public record DecisionRequest(String decision, String authorityBasis) {}
  @PostMapping("/resource-requests/{rid}/allocate")
  Map<String,Object> allocate(@PathVariable UUID rid, @RequestBody DecisionRequest r) {
    var p = principal();
    var rq = resources.findById(rid).orElseThrow(() -> BizException.notFound("request_not_found","الطلب غير موجود"));
    if (r.authorityBasis() == null || r.authorityBasis().isBlank())
      throw BizException.badRequest("authority_required","تخصيص الموارد بقرار موثق بسند صلاحية — لا تخصيص شفهيًا في غرفة عمليات");
    decisions.save(new DisasterDecision(UUID.fromString(p.facilityId()), p.governorate(), rq.getEventId(), r.decision(), p.email(), r.authorityBasis()));
    rq.setStatus("allocated"); resources.save(rq);
    audit.record("disaster.resource.allocated","resource_request",rid.toString(),"{}");
    return Map.of("ok",true,"resource_request",rq);
  }
  @PostMapping("/resource-requests/{rid}/deliver")
  Map<String,Object> deliver(@PathVariable UUID rid) {
    principal();
    var rq = resources.findById(rid).orElseThrow(() -> BizException.notFound("request_not_found","الطلب غير موجود"));
    if (!DisasterRules.deliveryPermitted(rq.getStatus()))
      throw BizException.conflict("not_allocated","التسليم ضد تخصيص موثق فقط — لا يسبق التسليمُ القرارَ");
    rq.setStatus("delivered"); resources.save(rq);
    audit.record("disaster.resource.delivered","resource_request",rid.toString(),"{}");
    return Map.of("ok",true,"resource_request",rq);
  }
  @PostMapping("/events/{id}/close")
  Map<String,Object> close(@PathVariable UUID id) {
    principal(); var e = event(id);
    long open = resources.countByEventIdAndStatusNot(id, "delivered");
    if (!DisasterRules.eventClosable(open))
      throw BizException.conflict("open_requests","لا إغلاق للحدث وطلبات موارد لم تُسلَّم — منشأة ما زالت تنتظر ما وُعدت به");
    e.close(); events.save(e);
    return Map.of("ok",true,"event",e);
  }
}
