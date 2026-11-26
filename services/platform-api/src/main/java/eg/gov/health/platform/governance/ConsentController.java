package eg.gov.health.platform.governance;
import eg.gov.health.platform.common.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.OffsetDateTime; import java.util.*;
@RestController @RequestMapping("/api/consents")
public class ConsentController {
  private final ConsentRecordRepository consents; private final BreakGlassAccessRepository breakGlass; private final AuditService audit;
  public ConsentController(ConsentRecordRepository consents, BreakGlassAccessRepository breakGlass, AuditService audit) {
    this.consents=consents; this.breakGlass=breakGlass; this.audit=audit;
  }
  public record ConsentRequest(UUID patientId, String purpose, String recipient, String dataScope, String consentVersion, OffsetDateTime expiresAt) {}
  @PostMapping
  Map<String,Object> grant(@RequestBody ConsentRequest r) {
    var p = TenantContext.get();
    if (r.patientId()==null||r.purpose()==null||r.recipient()==null||r.dataScope()==null)
      throw BizException.badRequest("missing_field","patientId و purpose و recipient و dataScope مطلوبة — الموافقة بلا نطاق لا قيمة لها");
    var c = consents.save(new ConsentRecord(p!=null&&p.facilityId()!=null?UUID.fromString(p.facilityId()):null,
      p==null?null:p.governorate(), r.patientId(), r.purpose(), r.recipient(), r.dataScope(), r.consentVersion(), r.expiresAt()));
    audit.record("consent.granted","consent",c.getId().toString(),"{}");
    return Map.of("ok",true,"consent",c);
  }
  @PostMapping("/{id}/revoke")
  Map<String,Object> revoke(@PathVariable UUID id) {
    var c = consents.findById(id).orElseThrow(() -> BizException.notFound("consent_not_found","الموافقة غير موجودة"));
    c.revoke(); consents.save(c);
    audit.record("consent.revoked","consent",id.toString(),"{}");
    return Map.of("ok",true,"consent",c,"note","السحب يوقف الاستخدامات المستقبلية ولا يمحو معالجة سابقة قامت على أساس قانوني (دورة 218)");
  }
  @GetMapping("/check")
  Map<String,Object> check(@RequestParam UUID patientId, @RequestParam String purpose, @RequestParam String recipient, @RequestParam String dataScope) {
    var now = OffsetDateTime.now();
    boolean allowed = consents.findByPatientIdAndPurposeAndRecipientAndDataScopeAndStatus(patientId, purpose, recipient, dataScope, "active")
      .stream().anyMatch(c -> ConsentRules.isShareAllowed(
        new ConsentRules.ConsentView(c.getPurpose(), c.getRecipient(), c.getDataScope(), c.getStatus(), c.getEffectiveFrom(), c.getExpiresAt()),
        purpose, recipient, dataScope, now));
    audit.record(allowed?"consent.share.allowed":"consent.share.denied","patient",patientId.toString(),"{}");
    return Map.of("ok",true,"share_allowed",allowed);
  }
  @GetMapping("/patient/{patientId}")
  Map<String,Object> byPatient(@PathVariable UUID patientId) {
    var rows = consents.findByPatientIdOrderByCreatedAtDesc(patientId);
    return Map.of("ok",true,"consents",rows,"count",rows.size());
  }
  public record BreakGlassRequest(UUID patientId, String reason, Integer durationMinutes) {}
  @PostMapping("/break-glass")
  Map<String,Object> breakGlass(@RequestBody BreakGlassRequest r) {
    var p = TenantContext.get();
    int mins = r.durationMinutes()==null?60:r.durationMinutes();
    if (r.patientId()==null || !ConsentRules.isBreakGlassJustifiable(r.reason(), mins))
      throw BizException.badRequest("unjustified_break_glass","الوصول الاستثنائي يتطلب سببًا موثقًا (10 أحرف فأكثر) ومدة لا تتجاوز 240 دقيقة");
    var bg = breakGlass.save(new BreakGlassAccess(p!=null&&p.facilityId()!=null?UUID.fromString(p.facilityId()):null,
      p==null?null:p.governorate(), r.patientId(), p==null?null:p.userId(), r.reason(), mins));
    audit.record("consent.break_glass.opened","break_glass",bg.getId().toString(),"{}");
    return Map.of("ok",true,"break_glass",bg,"note","يخضع لمراجعة لاحقة إلزامية من مسؤول الخصوصية");
  }
  @GetMapping("/break-glass/pending-review") @PreAuthorize("hasAnyRole('national_admin','governorate_admin')")
  Map<String,Object> pendingReview() {
    var rows = breakGlass.findByStatusOrderByCreatedAtAsc("active");
    return Map.of("ok",true,"pending_review",rows,"count",rows.size());
  }
  @PostMapping("/break-glass/{id}/review") @PreAuthorize("hasAnyRole('national_admin','governorate_admin')")
  Map<String,Object> review(@PathVariable UUID id, @RequestBody Map<String,String> body) {
    var p = TenantContext.get();
    var bg = breakGlass.findById(id).orElseThrow(() -> BizException.notFound("break_glass_not_found","غير موجود"));
    bg.review(p==null?null:p.userId(), body.getOrDefault("outcome","reviewed")); breakGlass.save(bg);
    audit.record("consent.break_glass.reviewed","break_glass",id.toString(),"{}");
    return Map.of("ok",true,"break_glass",bg);
  }
}
