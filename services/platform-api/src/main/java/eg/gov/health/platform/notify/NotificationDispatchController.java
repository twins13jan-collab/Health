package eg.gov.health.platform.notify;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
/** Notification outbox API (completion batch 2): queue a
 *  message on a real channel, dispatch the queue with recorded
 *  attempts, and read a recipient's in-app inbox. */
@RestController @RequestMapping("/api/notification-outbox")
public class NotificationDispatchController {
  private final NotificationService service; private final NotificationOutboxRepository outbox; private final NotificationAttemptRepository attempts;
  public NotificationDispatchController(NotificationService service, NotificationOutboxRepository outbox, NotificationAttemptRepository attempts) {
    this.service=service; this.outbox=outbox; this.attempts=attempts;
  }
  private TenantContext.Principal principal() {
    var p = TenantContext.get();
    if (p==null||p.facilityId()==null) throw BizException.forbidden("يتطلب حساب منشأة");
    return p;
  }
  public record QueueBody(String recipientRef, String channel, String subject, String body) {}
  @PostMapping
  Map<String,Object> queue(@RequestBody QueueBody r) {
    var p = principal();
    if (!NotificationRules.knownChannel(r.channel()))
      throw BizException.badRequest("unknown_channel","قناة غير معروفة: " + r.channel() + " — القنوات: in_app, webhook, sms, email");
    var n = service.queue(UUID.fromString(p.facilityId()), p.governorate(), r.recipientRef(), r.channel(), r.subject(), r.body());
    return Map.of("ok",true,"notification",n,"status","queued");
  }
  @PostMapping("/dispatch")
  Map<String,Object> dispatch() {
    principal();
    return Map.of("ok",true,"result",service.dispatchQueued());
  }
  @GetMapping("/inbox/{recipientRef}")
  Map<String,Object> inbox(@PathVariable String recipientRef) {
    principal();
    return Map.of("ok",true,"messages",outbox.findByRecipientRefAndChannelOrderByCreatedAtDesc(recipientRef, "in_app"));
  }
  @GetMapping("/{id}/attempts")
  Map<String,Object> attemptsOf(@PathVariable UUID id) {
    principal();
    return Map.of("ok",true,"attempts",attempts.findByNotificationIdOrderByCreatedAtAsc(id));
  }
}
