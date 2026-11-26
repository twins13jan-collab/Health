package eg.gov.health.platform.observability;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** National observability & operational transparency
 *  (module 62). The health of services, APIs, databases,
 *  messages, integrations and edge devices is monitored;
 *  response times, error rates, delays, queue backlogs and
 *  resource consumption are watched; alerts, operational
 *  incidents, escalation and the resolution record are managed;
 *  data freshness, quality, source and confidence are tracked;
 *  and technical operations dashboards stay separate from
 *  management indicator dashboards. */
@RestController @RequestMapping("/api/observability")
public class ObservabilityController {
  private final ObservedComponentRepository components; private final ComponentMetricRepository metrics;
  private final OpsIncidentRepository incidents; private final AuditService audit;
  public ObservabilityController(ObservedComponentRepository components, ComponentMetricRepository metrics,
                                 OpsIncidentRepository incidents, AuditService audit) {
    this.components=components; this.metrics=metrics; this.incidents=incidents; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private ObservedComponent component(UUID id) {
    return components.findById(id).orElseThrow(() -> BizException.notFound("component_not_found","المكون غير موجود"));
  }
  private OpsIncident incident(UUID id) {
    return incidents.findById(id).orElseThrow(() -> BizException.notFound("incident_not_found","الحادث غير موجود"));
  }

  public record ComponentBody(String componentRef, String componentType) {}
  @PostMapping("/components")
  Map<String,Object> create(@RequestBody ComponentBody r) {
    var p = principal();
    var c = components.save(new ObservedComponent(UUID.fromString(p.facilityId()), p.governorate(), r.componentRef(), r.componentType()));
    return Map.of("ok",true,"component",c);
  }
  public record MetricBody(String metric, Double value, Double threshold) {}
  @PostMapping("/components/{id}/metrics")
  Map<String,Object> metric(@PathVariable UUID id, @RequestBody MetricBody r) {
    var p = principal(); component(id);
    var m = metrics.save(new ComponentMetric(UUID.fromString(p.facilityId()), p.governorate(), id, r.metric(), r.value(), r.threshold()));
    boolean breached = ObservabilityRules.breached(r.value(), r.threshold());
    if (breached) audit.record("observability.threshold.breached","component_metric",m.getId().toString(),"{}");
    return Map.of("ok",true,"metric_record",m,"breached",breached,"note","a breach is surfaced, never averaged away");
  }
  public record IncidentBody(String severity) {}
  @PostMapping("/components/{id}/incidents")
  Map<String,Object> incident(@PathVariable UUID id, @RequestBody IncidentBody r) {
    var p = principal(); component(id);
    var i = incidents.save(new OpsIncident(UUID.fromString(p.facilityId()), p.governorate(), id, r.severity()));
    return Map.of("ok",true,"incident",i);
  }
  @PostMapping("/incidents/{id}/escalate")
  Map<String,Object> escalate(@PathVariable UUID id) {
    principal(); var i = incident(id);
    i.escalate(); incidents.save(i);
    return Map.of("ok",true,"incident",i);
  }
  public record CloseBody(String resolutionNote) {}
  @PostMapping("/incidents/{id}/close")
  Map<String,Object> close(@PathVariable UUID id, @RequestBody CloseBody r) {
    principal(); var i = incident(id);
    if (!ObservabilityRules.closable(i.getSeverity(), i.getStatus(), r.resolutionNote()))
      throw BizException.conflict("not_closable","حادث تشغيلي لا يُغلق بلا سجل معالجة — والحادث الحرج يمر بالتصعيد قبل الإغلاق");
    i.close(r.resolutionNote()); incidents.save(i);
    audit.record("observability.incident.closed","ops_incident",id.toString(),"{}");
    return Map.of("ok",true,"incident",i);
  }
}
