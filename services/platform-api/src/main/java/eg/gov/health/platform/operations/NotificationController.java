package eg.gov.health.platform.operations;
import eg.gov.health.platform.common.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
@RestController @RequestMapping("/api/notifications")
public class NotificationController {
  private final NotificationRepository repo;
  public NotificationController(NotificationRepository repo) { this.repo=repo; }
  public record NotifyRequest(String recipient, String channel, String subject, String body) {}
  @PostMapping
  Map<String,Object> send(@RequestBody NotifyRequest r) {
    var p = TenantContext.get();
    if (r.recipient()==null||r.subject()==null||r.body()==null) throw BizException.badRequest("missing_field","recipient و subject و body مطلوبة");
    var n = repo.save(new Notification(p!=null&&p.facilityId()!=null?UUID.fromString(p.facilityId()):null,
      p==null?null:p.governorate(), r.recipient(), r.channel(), r.subject(), r.body()));
    // Outbox pattern: queued here, delivered by the messaging worker (Kafka) —
    // the API never blocks on SMS/email gateways.
    return Map.of("ok",true,"notification",n);
  }
  @GetMapping("/mine")
  Map<String,Object> mine() {
    var p = TenantContext.get();
    var rows = repo.findByRecipientOrderByCreatedAtDesc(p==null?"":p.userId());
    return Map.of("ok",true,"notifications",rows,"count",rows.size());
  }
}
