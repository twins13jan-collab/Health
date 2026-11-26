package eg.gov.health.platform.quality;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @RequestMapping("/api/incidents")
public class IncidentController {
  private final IncidentReportRepository repo; private final AuditService audit;
  public IncidentController(IncidentReportRepository repo, AuditService audit) { this.repo=repo; this.audit=audit; }
  static final List<String> SEVERITIES = List.of("near_miss","minor","moderate","major","catastrophic");
  public record IncidentRequest(UUID patientId, String category, String severity, String description) {}
  @PostMapping
  Map<String,Object> report(@RequestBody IncidentRequest r) {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    if (r.category()==null||!SEVERITIES.contains(r.severity()==null?"":r.severity())||r.description()==null||r.description().isBlank())
      throw BizException.badRequest("invalid_incident","فئة وخطورة معروفة ووصف مطلوبة");
    var inc = repo.save(new IncidentReport(UUID.fromString(p.facilityId()), p.governorate(), r.patientId(), r.category(), r.severity(), r.description(), p.userId()));
    audit.record("incident.reported","incident",inc.getId().toString(),"{}");
    var out = new HashMap<String,Object>(); out.put("ok",true); out.put("incident",inc);
    if (List.of("major","catastrophic").contains(r.severity()))
      out.put("escalation","حدث جسيم — إخطار فوري لإدارة الجودة والسلامة ومدير المنشأة");
    return out;
  }
  @PostMapping("/{id}/review")
  Map<String,Object> review(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var inc = repo.findById(id).orElseThrow(() -> BizException.notFound("incident_not_found","البلاغ غير موجود"));
    if (body.getOrDefault("root_cause","").isBlank() || body.getOrDefault("corrective_action","").isBlank())
      throw BizException.badRequest("review_incomplete","تحليل السبب الجذري والإجراء التصحيحي إلزاميان قبل التقدم");
    inc.review(body.get("root_cause"), body.get("corrective_action")); repo.save(inc);
    audit.record("incident.reviewed","incident",id.toString(),"{}");
    return Map.of("ok",true,"incident",inc);
  }
  @PostMapping("/{id}/close")
  Map<String,Object> close(@PathVariable UUID id) {
    var inc = repo.findById(id).orElseThrow(() -> BizException.notFound("incident_not_found","البلاغ غير موجود"));
    if (!"action_assigned".equals(inc.getStatus())) throw BizException.conflict("not_reviewed","لا يُغلق بلاغ قبل المراجعة وتعيين إجراء تصحيحي");
    inc.close(); repo.save(inc);
    audit.record("incident.closed","incident",id.toString(),"{}");
    return Map.of("ok",true,"incident",inc);
  }
  @GetMapping
  Map<String,Object> list(@RequestParam(required=false) String status) {
    var p = TenantContext.get();
    var rows = status==null ? repo.findByFacilityIdOrderByCreatedAtDesc(UUID.fromString(p.facilityId()))
                            : repo.findByFacilityIdAndStatus(UUID.fromString(p.facilityId()), status);
    return Map.of("ok",true,"incidents",rows,"count",rows.size());
  }
}
