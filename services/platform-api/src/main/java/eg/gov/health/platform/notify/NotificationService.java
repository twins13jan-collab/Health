package eg.gov.health.platform.notify;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.URI; import java.net.http.*; import java.time.Duration;
import java.util.*;
/** The dispatcher. in_app delivers into the recipient's
 *  in-system inbox; webhook performs a REAL HTTP POST to the
 *  recipient-configured endpoint; sms and email POST to a
 *  configured gateway URL (app.notify.sms-gateway /
 *  app.notify.email-gateway). A channel whose gateway is not
 *  configured fails honestly as gateway_not_configured and is
 *  retried up to the attempt cap, then marked dead — never
 *  silently dropped, never fake-sent. */
@Service
public class NotificationService {
  private final NotificationOutboxRepository outbox; private final NotificationAttemptRepository attempts;
  @Value("${app.notify.sms-gateway:}") private String smsGateway;
  @Value("${app.notify.email-gateway:}") private String emailGateway;
  private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
  public NotificationService(NotificationOutboxRepository outbox, NotificationAttemptRepository attempts) {
    this.outbox=outbox; this.attempts=attempts;
  }
  public NotificationOutbox queue(UUID facilityId, String governorate, String recipientRef, String channel, String subject, String body) {
    return outbox.save(new NotificationOutbox(facilityId, governorate, recipientRef, channel, subject, body));
  }
  public Map<String,Object> dispatchQueued() {
    int sent=0, failed=0, dead=0;
    for (var n : outbox.findByStatusOrderByCreatedAtAsc("queued")) {
      if (!NotificationRules.dispatchable(n.getStatus(), n.getAttempts())) continue;
      String outcome; String detail;
      try {
        switch (n.getChannel()) {
          case "in_app" -> { outcome="delivered"; detail="in-app inbox"; }
          case "webhook" -> { detail = post(n.getRecipientRef(), n); outcome="delivered"; }
          case "sms" -> { if (smsGateway.isBlank()) throw new IllegalStateException("gateway_not_configured"); detail = post(smsGateway, n); outcome="delivered"; }
          case "email" -> { if (emailGateway.isBlank()) throw new IllegalStateException("gateway_not_configured"); detail = post(emailGateway, n); outcome="delivered"; }
          default -> throw new IllegalStateException("unknown_channel");
        }
        n.markSent(); outbox.save(n);
        attempts.save(new NotificationAttempt(n.getFacilityId(), n.getGovernorate(), n.getId(), n.getChannel(), outcome, detail));
        sent++;
      } catch (Exception e) {
        String err = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
        n.markFailed(err); outbox.save(n);
        attempts.save(new NotificationAttempt(n.getFacilityId(), n.getGovernorate(), n.getId(), n.getChannel(), "failed", err));
        if ("dead".equals(n.getStatus())) dead++; else failed++;
      }
    }
    return Map.of("sent",sent,"failed_retryable",failed,"dead",dead);
  }
  private String post(String url, NotificationOutbox n) throws Exception {
    String json = "{\"to\":\"" + escape(n.getRecipientRef()) + "\",\"subject\":\"" + escape(n.getSubject()) + "\",\"body\":\"" + escape(n.getBody()) + "\"}";
    var req = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(8))
      .header("Content-Type","application/json").POST(HttpRequest.BodyPublishers.ofString(json)).build();
    var resp = http.send(req, HttpResponse.BodyHandlers.ofString());
    if (resp.statusCode() >= 300) throw new IllegalStateException("gateway_http_" + resp.statusCode());
    return "http " + resp.statusCode();
  }
  private String escape(String s) { return s == null ? "" : s.replace("\\","\\\\").replace("\"","\\\""); }
}
