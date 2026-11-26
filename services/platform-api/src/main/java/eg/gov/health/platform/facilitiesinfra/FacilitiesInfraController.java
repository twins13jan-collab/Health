package eg.gov.health.platform.facilitiesinfra;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Facilities, energy & infrastructure (module 108). Buildings,
 *  rooms, electromechanical systems and medical gases; energy,
 *  water, fuel, cooling and ventilation monitored; maintenance
 *  and work orders managed; critical failures handled with
 *  fallback plans; and air quality, pressure and temperatures
 *  watched. */
@RestController @RequestMapping("/api/facilities-infra")
public class FacilitiesInfraController {
  private final InfraSystemRepository systems; private final InfraReadingRepository readings;
  private final InfraWorkOrderRepository orders; private final AuditService audit;
  public FacilitiesInfraController(InfraSystemRepository systems, InfraReadingRepository readings, InfraWorkOrderRepository orders, AuditService audit) {
    this.systems=systems; this.readings=readings; this.orders=orders; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private InfraSystem system(UUID id) {
    return systems.findById(id).orElseThrow(() -> BizException.notFound("system_not_found","النظام غير موجود"));
  }

  public record SystemBody(String systemType, String name, Boolean critical) {}
  @PostMapping("/systems")
  Map<String,Object> create(@RequestBody SystemBody r) {
    var p = principal();
    var s = systems.save(new InfraSystem(UUID.fromString(p.facilityId()), p.governorate(), r.systemType(), r.name(), Boolean.TRUE.equals(r.critical())));
    return Map.of("ok",true,"system",s);
  }
  public record ReadingBody(String metric, Double value, Boolean withinLimits) {}
  @PostMapping("/systems/{id}/readings")
  Map<String,Object> reading(@PathVariable UUID id, @RequestBody ReadingBody r) {
    var p = principal(); var s = system(id);
    boolean within = Boolean.TRUE.equals(r.withinLimits());
    var rd = readings.save(new InfraReading(UUID.fromString(p.facilityId()), p.governorate(), id, r.metric(), r.value(), within));
    if (!within) { s.setStatus("degraded"); systems.save(s); audit.record("infra.reading.breach","infra_reading",rd.getId().toString(),"{}"); }
    return Map.of("ok",true,"reading",rd,"system_status",within ? "operational" : "degraded");
  }
  public record OrderBody(String criticality, String description, String fallbackPlan) {}
  @PostMapping("/systems/{id}/work-orders")
  Map<String,Object> order(@PathVariable UUID id, @RequestBody OrderBody r) {
    var p = principal(); system(id);
    var o = orders.save(new InfraWorkOrder(UUID.fromString(p.facilityId()), p.governorate(), id, r.criticality(), r.description(), r.fallbackPlan()));
    return Map.of("ok",true,"work_order",o);
  }
  @PostMapping("/work-orders/{oid}/close")
  Map<String,Object> closeOrder(@PathVariable UUID oid) {
    principal();
    var o = orders.findById(oid).orElseThrow(() -> BizException.notFound("order_not_found","أمر العمل غير موجود"));
    var s = system(o.getSystemId());
    var latest = readings.findBySystemIdOrderByCreatedAtDesc(o.getSystemId());
    boolean withinLimits = !latest.isEmpty() && latest.get(0).isWithinLimits();
    if (!InfraRules.workOrderClosable(s.isCritical(), o.getCriticality(), o.getFallbackPlan(), withinLimits))
      throw BizException.conflict("closure_blocked","أمر عمل حرج لا يُغلق بلا خطة بديل قائمة وقراءة ما بعد إصلاح داخل الحدود — الإصلاح الورقي لا يعيد الأكسجين");
    o.close(); orders.save(o);
    s.setStatus("operational"); systems.save(s);
    audit.record("infra.work_order.closed","infra_work_order",oid.toString(),"{}");
    return Map.of("ok",true,"work_order",o);
  }
}
