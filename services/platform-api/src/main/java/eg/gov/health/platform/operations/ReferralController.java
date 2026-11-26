package eg.gov.health.platform.operations;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @RequestMapping("/api/referrals")
public class ReferralController {
  private final ReferralRepository repo; private final AuditService audit;
  public ReferralController(ReferralRepository repo, AuditService audit) { this.repo=repo; this.audit=audit; }
  static final Map<String,List<String>> FLOW = Map.of(
    "pending", List.of("accepted","rejected","cancelled"),
    "accepted", List.of("in_transit","cancelled"),
    "in_transit", List.of("arrived"),
    "arrived", List.of("completed"),
    "rejected", List.of(), "completed", List.of(), "cancelled", List.of());
  public record ReferralRequest(UUID patientId, UUID toFacilityId, String reason, String urgency) {}
  @PostMapping
  Map<String,Object> create(@RequestBody ReferralRequest r) {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    if (r.patientId()==null||r.toFacilityId()==null||r.reason()==null) throw BizException.badRequest("missing_field","patientId و toFacilityId و reason مطلوبة");
    if (r.toFacilityId().toString().equals(p.facilityId())) throw BizException.badRequest("same_facility","الإحالة لمنشأة أخرى");
    var ref = repo.save(new Referral(UUID.fromString(p.facilityId()), p.governorate(), r.patientId(), r.toFacilityId(), r.reason(), r.urgency()));
    audit.record("referral.created","referral",ref.getId().toString(),"{}");
    return Map.of("ok",true,"referral",ref);
  }
  @PostMapping("/{id}/accept")
  Map<String,Object> accept(@PathVariable UUID id) {
    var p = TenantContext.get(); var ref = get(id);
    if (!ref.getToFacilityId().toString().equals(p.facilityId())) throw BizException.forbidden("القبول من المنشأة المستقبلة فقط (دورة 224)");
    if (!"pending".equals(ref.getStatus())) throw BizException.conflict("not_pending","الإحالة ليست معلقة");
    ref.accept(p.userId()); repo.save(ref);
    audit.record("referral.accepted","referral",id.toString(),"{}");
    return Map.of("ok",true,"referral",ref);
  }
  @PostMapping("/{id}/reject")
  Map<String,Object> reject(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var p = TenantContext.get(); var ref = get(id);
    if (!ref.getToFacilityId().toString().equals(p.facilityId())) throw BizException.forbidden("الرفض من المنشأة المستقبلة فقط");
    if (body.getOrDefault("reason","").isBlank()) throw BizException.badRequest("reason_required","سبب الرفض موثق إلزاميًا");
    if (!"pending".equals(ref.getStatus())) throw BizException.conflict("not_pending","الإحالة ليست معلقة");
    ref.reject(body.get("reason")); repo.save(ref);
    audit.record("referral.rejected","referral",id.toString(),"{}");
    return Map.of("ok",true,"referral",ref);
  }
  @PostMapping("/{id}/status")
  Map<String,Object> status(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var ref = get(id); String to = body.getOrDefault("status","");
    if (!FLOW.getOrDefault(ref.getStatus(), List.of()).contains(to))
      throw BizException.conflict("illegal_transition","انتقال غير مسموح: "+ref.getStatus()+" -> "+to);
    ref.setStatus(to); repo.save(ref);
    audit.record("referral."+to,"referral",id.toString(),"{}");
    return Map.of("ok",true,"referral",ref);
  }
  @GetMapping("/incoming")
  Map<String,Object> incoming() {
    var p = TenantContext.get();
    var rows = repo.findByToFacilityIdAndStatus(UUID.fromString(p.facilityId()), "pending");
    return Map.of("ok",true,"referrals",rows,"count",rows.size());
  }
  @GetMapping
  Map<String,Object> list() {
    var p = TenantContext.get();
    var rows = repo.findByFacilityIdOrderByCreatedAtDesc(UUID.fromString(p.facilityId()));
    return Map.of("ok",true,"referrals",rows,"count",rows.size());
  }
  private Referral get(UUID id) { return repo.findById(id).orElseThrow(() -> BizException.notFound("referral_not_found","الإحالة غير موجودة")); }
}
