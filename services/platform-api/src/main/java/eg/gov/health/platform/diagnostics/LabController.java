package eg.gov.health.platform.diagnostics;
import eg.gov.health.platform.common.*;
import jakarta.transaction.Transactional;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal; import java.util.*;
@RestController @RequestMapping("/api/labs")
public class LabController {
  private final LabOrderRepository orders; private final LabResultRepository results; private final AuditService audit;
  public LabController(LabOrderRepository orders, LabResultRepository results, AuditService audit) { this.orders=orders; this.results=results; this.audit=audit; }
  static final List<String> FLOW = List.of("ordered","specimen_collected","received","in_analysis","resulted","verified","corrected","cancelled");
  public record OrderRequest(UUID patientId, UUID encounterId, String testCode, String testName, String priority) {}
  @PostMapping("/orders")
  Map<String,Object> create(@RequestBody OrderRequest r) {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    if (r.patientId()==null||r.testCode()==null) throw BizException.badRequest("missing_field","patientId و testCode مطلوبان");
    var o = orders.save(new LabOrder(UUID.fromString(p.facilityId()), p.governorate(), r.patientId(), r.encounterId(), r.testCode(), r.testName(), r.priority(), p.userId()));
    audit.record("lab.order.created","lab_order",o.getId().toString(),"{}");
    return Map.of("ok",true,"order",o);
  }
  @GetMapping("/orders")
  Map<String,Object> list(@RequestParam(required=false) UUID patientId, @RequestParam(required=false) Integer page, @RequestParam(required=false) Integer size) {
    var p = TenantContext.get();
    var rows = patientId!=null ? orders.findByPatientIdOrderByCreatedAtDesc(patientId) : orders.findByFacilityIdOrderByCreatedAtDesc(UUID.fromString(p.facilityId()));
    return Paging.page(rows, "orders", page, size);
  }
  @PostMapping("/orders/{id}/status")
  Map<String,Object> status(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var o = orders.findById(id).orElseThrow(() -> BizException.notFound("order_not_found","الطلب غير موجود"));
    String to = body.getOrDefault("status","");
    int cur = FLOW.indexOf(o.getStatus()); int nxt = FLOW.indexOf(to);
    if (nxt < 0) throw BizException.badRequest("invalid_status","حالة غير معروفة");
    // Forward-only along the documented flow; cancellation allowed any time before verified.
    if (!("cancelled".equals(to) && cur < FLOW.indexOf("verified")) && nxt != cur + 1)
      throw BizException.conflict("illegal_transition","لا يمكن الانتقال من "+o.getStatus()+" إلى "+to+" — المسار التسلسلي إلزامي (دورة 219)");
    o.setStatus(to); orders.save(o);
    audit.record("lab.order."+to,"lab_order",id.toString(),"{}");
    return Map.of("ok",true,"order",o);
  }
  public record ResultRequest(BigDecimal value, String unit, BigDecimal refLow, BigDecimal refHigh, UUID supersedesId) {}
  @PostMapping("/orders/{id}/results") @Transactional
  Map<String,Object> result(@PathVariable UUID id, @RequestBody ResultRequest r) {
    var p = TenantContext.get();
    var o = orders.findById(id).orElseThrow(() -> BizException.notFound("order_not_found","الطلب غير موجود"));
    if (r.value()==null) throw BizException.badRequest("missing_field","value مطلوبة");
    boolean critical = CriticalValueRules.isCritical(o.getTestCode(), r.value());
    var res = new LabResult(o.getFacilityId(), o.getGovernorate(), id, r.value(), r.unit(), r.refLow(), r.refHigh(), critical, r.supersedesId());
    if (r.supersedesId()!=null) {
      var prev = results.findById(r.supersedesId()).orElseThrow(() -> BizException.notFound("result_not_found","النتيجة السابقة غير موجودة"));
      res.setVersion(prev.getVersion()+1); // correction = NEW version; the old one is never overwritten (cycle 219)
    }
    results.save(res);
    if (!"resulted".equals(o.getStatus()) && !"verified".equals(o.getStatus())) { o.setStatus("resulted"); orders.save(o); }
    audit.record(critical ? "lab.result.critical" : "lab.result.created","lab_result",res.getId().toString(),"{}");
    var out = new HashMap<String,Object>(); out.put("ok",true); out.put("result",res); out.put("critical",critical);
    if (critical) out.put("alert","نتيجة حرجة — يجب الإقرار بالاستلام من الطبيب المعالج أو التصعيد للبديل المحدد (دورة 219)");
    return out;
  }
  @PostMapping("/results/{id}/verify")
  Map<String,Object> verify(@PathVariable UUID id) {
    var p = TenantContext.get();
    var res = results.findById(id).orElseThrow(() -> BizException.notFound("result_not_found","النتيجة غير موجودة"));
    res.verify(p==null?null:p.userId()); results.save(res);
    audit.record("lab.result.verified","lab_result",id.toString(),"{}");
    return Map.of("ok",true,"result",res);
  }
  @GetMapping("/critical-alerts")
  Map<String,Object> criticalAlerts() {
    var p = TenantContext.get();
    var rows = results.findByFacilityIdAndCriticalTrueAndStatusNot(UUID.fromString(p.facilityId()), "verified");
    return Map.of("ok",true,"critical_alerts",rows,"count",rows.size());
  }
}
