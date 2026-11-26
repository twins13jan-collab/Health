package eg.gov.health.platform.servicedesk;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.time.*; import java.util.*;
/** Service centre & technical support (module 139). User reports
 *  are received tied to their service, facility and device —
 *  hardware, connectivity, printing, barcode, software, accounts —
 *  classified, escalated and resolved against severity SLAs, with
 *  response time and satisfaction measured. Security incidents and
 *  clinical-safety reports are separated into their own tracks and
 *  resolved only by their own authorities. Proven fixes feed the
 *  knowledge base. */
@RestController @RequestMapping("/api/service-desk")
public class ServiceDeskController {
  private final SupportTicketRepository tickets; private final KbArticleRepository kb; private final AuditService audit;
  public ServiceDeskController(SupportTicketRepository tickets, KbArticleRepository kb, AuditService audit) {
    this.tickets=tickets; this.kb=kb; this.audit=audit;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  private SupportTicket ticket(UUID id) {
    return tickets.findById(id).orElseThrow(() -> BizException.notFound("ticket_not_found","البلاغ غير موجود"));
  }

  public record TicketRequest(String serviceRef, String deviceRef, String category, String severity, String description, OffsetDateTime createdAt) {}
  @PostMapping("/tickets")
  Map<String,Object> open(@RequestBody TicketRequest r) {
    var p = principal();
    var t = tickets.save(new SupportTicket(UUID.fromString(p.facilityId()), p.governorate(), p.email(), r.serviceRef(), r.deviceRef(),
      r.category(), r.severity(), r.description(), r.createdAt()));
    audit.record("desk.ticket.opened","support_ticket",t.getId().toString(),"{}");
    return Map.of("ok",true,"ticket",t,"track",t.getTrack(),
      "routing_note",switch (t.getTrack()) {
        case "security" -> "routed to the SECURITY track — resolved only by security authority";
        case "clinical" -> "routed to the CLINICAL track — resolved only by clinical authority";
        default -> "standard service-desk track"; });
  }
  public record RespondRequest(OffsetDateTime respondedAt) {}
  @PostMapping("/tickets/{id}/respond")
  Map<String,Object> respond(@PathVariable UUID id, @RequestBody RespondRequest r) {
    principal(); var t = ticket(id);
    t.respond(r.respondedAt() == null ? OffsetDateTime.now() : r.respondedAt()); tickets.save(t);
    return Map.of("ok",true,"ticket",t,
      "response_target_min",ServiceDeskRules.RESPONSE_TARGET_MIN.getOrDefault(t.getSeverity(), 480),
      "response_breached",ServiceDeskRules.responseBreached(t.getSeverity(), t.getCreatedAt(), t.getFirstResponseAt()));
  }
  @PostMapping("/tickets/{id}/escalate")
  Map<String,Object> escalate(@PathVariable UUID id) {
    principal(); var t = ticket(id);
    t.escalate(); tickets.save(t);
    audit.record("desk.ticket.escalated","support_ticket",id.toString(),"{}");
    return Map.of("ok",true,"ticket",t);
  }
  public record ResolveRequest(String resolution) {}
  @PostMapping("/tickets/{id}/resolve")
  Map<String,Object> resolve(@PathVariable UUID id, @RequestBody ResolveRequest r) {
    var p = principal(); var t = ticket(id);
    if (!ServiceDeskRules.mayResolve(t.getTrack(), p.role()))
      throw BizException.forbidden("بلاغات المسارات الخاصة تُحل فقط بسلطة مسارها (أمن/سريري)");
    if (r.resolution() == null || r.resolution().isBlank())
      throw BizException.badRequest("resolution_required","الإغلاق يتطلب حلًا موثقًا");
    t.resolve(r.resolution()); tickets.save(t);
    audit.record("desk.ticket.resolved","support_ticket",id.toString(),"{}");
    return Map.of("ok",true,"ticket",t);
  }
  public record RateRequest(Integer satisfactionScore) {}
  @PostMapping("/tickets/{id}/rate")
  Map<String,Object> rate(@PathVariable UUID id, @RequestBody RateRequest r) {
    principal(); var t = ticket(id);
    if (!"resolved".equals(t.getStatus())) throw BizException.conflict("not_resolved","الرضا يُقاس بعد الحل");
    t.rate(r.satisfactionScore() == null ? 0 : r.satisfactionScore()); tickets.save(t);
    return Map.of("ok",true,"ticket",t);
  }
  public record KbRequest(String title, String category, String solution, Boolean published) {}
  @PostMapping("/kb")
  Map<String,Object> kb(@RequestBody KbRequest r) {
    var p = principal();
    var a = kb.save(new KbArticle(UUID.fromString(p.facilityId()), p.governorate(), r.title(), r.category(), r.solution(), Boolean.TRUE.equals(r.published())));
    audit.record("desk.kb.published","kb_article",a.getId().toString(),"{}");
    return Map.of("ok",true,"article",a);
  }
}
