package eg.gov.health.platform.integration;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
@RestController @RequestMapping("/api/integration")
public class IntegrationController {
  private final AnalyticsClient analytics;
  public IntegrationController(AnalyticsClient analytics) { this.analytics = analytics; }
  @PostMapping("/triage-support")
  Map<String, Object> triage(@RequestBody Map<String, Object> vitals, HttpServletRequest req) {
    var out = analytics.triageSupport(vitals, String.valueOf(req.getAttribute("correlationId")));
    return Map.of("ok", true, "advisory_only", true, "requires_human_review", true, "analytics", out == null ? Map.of() : out);
  }
  @PostMapping("/capacity-forecast")
  Map<String, Object> capacity(@RequestBody Map<String, Object> series, HttpServletRequest req) {
    var out = analytics.capacityForecast(series, String.valueOf(req.getAttribute("correlationId")));
    return Map.of("ok", true, "advisory_only", true, "analytics", out == null ? Map.of() : out);
  }
}
