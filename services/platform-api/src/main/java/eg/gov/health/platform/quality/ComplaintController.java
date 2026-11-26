package eg.gov.health.platform.quality;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.OffsetDateTime; import java.util.*;
@RestController @RequestMapping("/api/complaints")
public class ComplaintController {
  private final ComplaintRepository repo; private final AuditService audit;
  public ComplaintController(ComplaintRepository repo, AuditService audit) { this.repo=repo; this.audit=audit; }
  public record ComplaintRequest(UUID patientId, String complainantName, String category, String severity, String description) {}
  @PostMapping
  Map<String,Object> file(@RequestBody ComplaintRequest r) {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    if (r.category()==null||r.description()==null||r.description().isBlank()) throw BizException.badRequest("missing_field","category و description مطلوبتان");
    var c = repo.save(new Complaint(UUID.fromString(p.facilityId()), p.governorate(), r.patientId(), r.complainantName(), r.category(), r.severity(), r.description()));
    audit.record("complaint.received","complaint",c.getId().toString(),"{}");
    return Map.of("ok",true,"complaint",c,"sla_due_at",c.getDueAt());
  }
  @PostMapping("/{id}/investigate")
  Map<String,Object> investigate(@PathVariable UUID id) {
    var c = get(id);
    if (!"received".equals(c.getStatus()) && !"escalated".equals(c.getStatus())) throw BizException.conflict("bad_state","التحقيق من حالة الاستلام أو التصعيد فقط");
    c.setStatus("investigating"); repo.save(c);
    return Map.of("ok",true,"complaint",c);
  }
  @PostMapping("/{id}/resolve")
  Map<String,Object> resolve(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var c = get(id);
    if (body.getOrDefault("resolution","").isBlank()) throw BizException.badRequest("resolution_required","نص الحل موثق إلزاميًا");
    if (!"investigating".equals(c.getStatus())) throw BizException.conflict("not_investigating","الحل بعد بدء التحقيق");
    c.resolve(body.get("resolution")); repo.save(c);
    audit.record("complaint.resolved","complaint",id.toString(),"{}");
    return Map.of("ok",true,"complaint",c);
  }
  @GetMapping("/sla-breaches")
  Map<String,Object> breaches() {
    var p = TenantContext.get(); var now = OffsetDateTime.now();
    var rows = repo.findByFacilityIdOrderByCreatedAtDesc(UUID.fromString(p.facilityId())).stream()
      .filter(c -> ComplaintSla.isBreached(c.getCreatedAt(), c.getSeverity(), c.getStatus(), now)).toList();
    return Map.of("ok",true,"breaches",rows,"count",rows.size());
  }
  @GetMapping
  Map<String,Object> list() {
    var p = TenantContext.get();
    var rows = repo.findByFacilityIdOrderByCreatedAtDesc(UUID.fromString(p.facilityId()));
    return Map.of("ok",true,"complaints",rows,"count",rows.size());
  }
  private Complaint get(UUID id) { return repo.findById(id).orElseThrow(() -> BizException.notFound("complaint_not_found","الشكوى غير موجودة")); }
}
