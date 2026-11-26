package eg.gov.health.platform.emergency;
import eg.gov.health.platform.common.*;
import jakarta.transaction.Transactional;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @RequestMapping("/api/dispatch")
public class DispatchController {
  private final AmbulanceUnitRepository units; private final DispatchRequestRepository requests; private final AuditService audit;
  public DispatchController(AmbulanceUnitRepository units, DispatchRequestRepository requests, AuditService audit) {
    this.units=units; this.requests=requests; this.audit=audit;
  }
  public record UnitReg(String unitCode, Double lat, Double lon, String capabilities) {}
  @PostMapping("/units")
  Map<String,Object> registerUnit(@RequestBody UnitReg r) {
    var p = TenantContext.get();
    if (r.unitCode()==null||r.lat()==null||r.lon()==null) throw BizException.badRequest("missing_field","unitCode و lat و lon مطلوبة");
    var u = units.save(new AmbulanceUnit(p!=null&&p.facilityId()!=null?UUID.fromString(p.facilityId()):null, p==null?null:p.governorate(), r.unitCode(), r.lat(), r.lon(), r.capabilities()));
    return Map.of("ok",true,"unit",u);
  }
  public record DispatchReq(UUID patientId, Double lat, Double lon, String complaint, String requiredCapability) {}
  @PostMapping("/requests") @Transactional
  Map<String,Object> request(@RequestBody DispatchReq r) {
    var p = TenantContext.get();
    if (r.lat()==null||r.lon()==null||r.complaint()==null) throw BizException.badRequest("missing_field","lat و lon و complaint مطلوبة");
    var req = requests.save(new DispatchRequest(p!=null&&p.facilityId()!=null?UUID.fromString(p.facilityId()):null,
      p==null?null:p.governorate(), r.patientId(), r.lat(), r.lon(), r.complaint(), r.requiredCapability()));
    var views = units.findByStatus("available").stream()
      .map(u -> new DispatchMath.UnitView(u.getUnitCode(), u.getLat(), u.getLon(), u.getStatus(), u.capabilitySet())).toList();
    var nearest = DispatchMath.nearestAvailable(r.lat(), r.lon(), r.requiredCapability(), views);
    var out = new HashMap<String,Object>(); out.put("ok",true); out.put("request",req);
    if (nearest.isPresent()) {
      var unit = units.findByUnitCode(nearest.get().unitCode()).orElseThrow();
      req.assign(unit.getId()); unit.setStatus("dispatched"); units.save(unit); requests.save(req);
      out.put("assigned_unit", unit.getUnitCode());
      out.put("distance_km", Math.round(DispatchMath.haversineKm(r.lat(), r.lon(), unit.getLat(), unit.getLon())*10.0)/10.0);
      audit.record("dispatch.assigned","dispatch_request",req.getId().toString(),"{}");
    } else {
      out.put("warning","لا توجد وحدة متاحة بالقدرة المطلوبة — يبقى الطلب في قائمة الانتظار للتصعيد (دورة 224)");
      audit.record("dispatch.unassigned","dispatch_request",req.getId().toString(),"{}");
    }
    return out;
  }
  @PostMapping("/requests/{id}/status")
  Map<String,Object> status(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var req = requests.findById(id).orElseThrow(() -> BizException.notFound("request_not_found","الطلب غير موجود"));
    String to = body.getOrDefault("status","");
    Map<String,List<String>> flow = Map.of(
      "received", List.of("assigned","cancelled"), "assigned", List.of("en_route","cancelled"),
      "en_route", List.of("on_scene"), "on_scene", List.of("transporting","completed"),
      "transporting", List.of("completed"), "completed", List.of(), "cancelled", List.of());
    if (!flow.getOrDefault(req.getStatus(), List.of()).contains(to))
      throw BizException.conflict("illegal_transition","انتقال غير مسموح: "+req.getStatus()+" -> "+to);
    req.setStatus(to); requests.save(req);
    audit.record("dispatch."+to,"dispatch_request",id.toString(),"{}");
    return Map.of("ok",true,"request",req);
  }
  @GetMapping("/queue")
  Map<String,Object> queue() {
    var rows = requests.findByStatusOrderByCreatedAtAsc("received");
    return Map.of("ok",true,"queue",rows,"count",rows.size());
  }
}
