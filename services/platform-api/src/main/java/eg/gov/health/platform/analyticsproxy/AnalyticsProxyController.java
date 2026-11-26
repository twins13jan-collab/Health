package eg.gov.health.platform.analyticsproxy;
import eg.gov.health.platform.common.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.net.URI; import java.net.http.HttpClient; import java.net.http.HttpResponse; import java.time.Duration;
/** Analytics & AI integration (completion batch 6). The
 *  Python analytics service is advisory-only by design; until
 *  now nothing in operations could reach it. This proxy puts
 *  its forecasts and triage support behind the platform's own
 *  authentication and tenancy, forwarding requests verbatim
 *  and returning its answers marked advisory. If the service
 *  is down the proxy says so (502 analytics_unavailable) —
 *  it never invents a forecast. */
@RestController @RequestMapping("/api/analytics")
public class AnalyticsProxyController {
  @Value("${app.analytics-ai-base-url:http://localhost:8000}") private String baseUrl;
  private final eg.gov.health.platform.aiadmin.AiRoutingService aiRouting;
  private final com.fasterxml.jackson.databind.ObjectMapper json = new com.fasterxml.jackson.databind.ObjectMapper();
  public AnalyticsProxyController(eg.gov.health.platform.aiadmin.AiRoutingService aiRouting) { this.aiRouting = aiRouting; }

  /** When the administrator has activated an external AI provider
   *  in the control panel, its configuration (including the
   *  decrypted key) is injected into the chat request here — inside
   *  the internal network only. The browser never sees it. */
  private String injectProvider(String body) {
    try {
      var cfg = aiRouting.activeConfig();
      if (cfg.isEmpty()) return body;
      var root = json.readValue(body == null || body.isBlank() ? "{}" : body, java.util.Map.class);
      var c = cfg.get();
      root.put("provider", java.util.Map.of(
          "name", c.name(), "baseUrl", c.baseUrl(), "model", c.model(),
          "apiKey", c.apiKey(), "authStyle", c.authStyle()));
      return json.writeValueAsString(root);
    } catch (Exception e) {
      return body;
    }
  }
  // HTTP_1_1 is pinned: the default HTTP/2 preference makes HttpClient send an
  // h2c upgrade request over cleartext, which uvicorn/h11 rejects as an
  // invalid request (this cost us the first EC2 deployment's analytics calls).
  private final HttpClient http = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).connectTimeout(Duration.ofSeconds(4)).build();
  private void principal() {
    if (TenantContext.get() == null) throw BizException.forbidden("يتطلب تسجيل الدخول");
  }
  private ResponseEntity<String> forward(String path, String jsonBody) {
    principal();
    if (baseUrl.startsWith("unix://")) {
      try {
        var r = UnixSocketHttp.request(baseUrl.substring("unix://".length()), "POST", path, jsonBody == null ? "{}" : jsonBody);
        return ResponseEntity.status(r.status()).contentType(MediaType.APPLICATION_JSON).body(r.body());
      } catch (Exception e) {
        return ResponseEntity.status(502).contentType(MediaType.APPLICATION_JSON)
          .body("{\"error\":\"analytics_unavailable\",\"message\":\"خدمة التحليلات غير متاحة الآن — لا تُعرض أي توقعات بديلة\"}");
      }
    }
    try {
      var req = java.net.http.HttpRequest.newBuilder(URI.create(baseUrl + path)).timeout(Duration.ofSeconds(15))
        .header("Content-Type","application/json").POST(java.net.http.HttpRequest.BodyPublishers.ofString(jsonBody == null ? "{}" : jsonBody)).build();
      var resp = http.send(req, HttpResponse.BodyHandlers.ofString());
      return ResponseEntity.status(resp.statusCode()).contentType(MediaType.APPLICATION_JSON).body(resp.body());
    } catch (Exception e) {
      return ResponseEntity.status(502).contentType(MediaType.APPLICATION_JSON)
        .body("{\"error\":\"analytics_unavailable\",\"message\":\"خدمة التحليلات غير متاحة الآن — لا تُعرض أي توقعات بديلة\"}");
    }
  }
  @PostMapping("/forecast/outbreak") ResponseEntity<String> outbreak(@RequestBody String body) { return forward("/v1/forecast/outbreak", body); }
  @PostMapping("/forecast/capacity") ResponseEntity<String> capacity(@RequestBody String body) { return forward("/v1/forecast/capacity", body); }
  @PostMapping("/amr/summary") ResponseEntity<String> amr(@RequestBody String body) { return forward("/v1/amr/summary", body); }
  @PostMapping("/ai/triage-support") ResponseEntity<String> triage(@RequestBody String body) { return forward("/v1/ai/triage-support", body); }
  /** Chat agent: the caller's Authorization is forwarded so the
   *  agent's scoped tools (ER board, clinic sessions) run under the
   *  caller's own permissions and tenancy — never wider. The LLM
   *  itself is a local open-source model inside the deployment. */
  @PostMapping("/ai/chat")
  ResponseEntity<String> chat(@RequestBody String body, @RequestHeader(value = "Authorization", required = false) String authorization) {
    principal();
    if (baseUrl.startsWith("unix://")) {
      // The unix-socket transport cannot carry headers; scoped tools
      // will honestly report not_authenticated in that topology.
      return forward("/v1/ai/chat", body);
    }
    body = injectProvider(body);
    try {
      var builder = java.net.http.HttpRequest.newBuilder(URI.create(baseUrl + "/v1/ai/chat"))
        .timeout(Duration.ofSeconds(240))
        .header("Content-Type", "application/json")
        .POST(java.net.http.HttpRequest.BodyPublishers.ofString(body == null ? "{}" : body));
      if (authorization != null) builder.header("Authorization", authorization);
      var resp = http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
      return ResponseEntity.status(resp.statusCode()).contentType(MediaType.APPLICATION_JSON).body(resp.body());
    } catch (Exception e) {
      return ResponseEntity.status(502).contentType(MediaType.APPLICATION_JSON)
        .body("{\"error\":\"analytics_unavailable\",\"message\":\"خدمة التحليلات غير متاحة الآن — لا تُعرض أي إجابة بديلة\"}");
    }
  }
  @GetMapping("/health")
  ResponseEntity<String> health() {
    principal();
    if (baseUrl.startsWith("unix://")) {
      try {
        var r = UnixSocketHttp.request(baseUrl.substring("unix://".length()), "GET", "/health", null);
        return ResponseEntity.status(r.status()).contentType(MediaType.APPLICATION_JSON).body(r.body());
      } catch (Exception e) {
        return ResponseEntity.status(502).contentType(MediaType.APPLICATION_JSON).body("{\"error\":\"analytics_unavailable\"}");
      }
    }
    try {
      var req = java.net.http.HttpRequest.newBuilder(URI.create(baseUrl + "/health")).timeout(Duration.ofSeconds(5)).GET().build();
      var resp = http.send(req, HttpResponse.BodyHandlers.ofString());
      return ResponseEntity.status(resp.statusCode()).contentType(MediaType.APPLICATION_JSON).body(resp.body());
    } catch (Exception e) {
      return ResponseEntity.status(502).contentType(MediaType.APPLICATION_JSON)
        .body("{\"error\":\"analytics_unavailable\"}");
    }
  }
}
