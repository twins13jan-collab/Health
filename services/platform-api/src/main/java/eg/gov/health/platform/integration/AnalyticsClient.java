package eg.gov.health.platform.integration;
import org.springframework.beans.factory.annotation.Value; import org.springframework.http.MediaType;
import org.springframework.stereotype.Service; import org.springframework.web.client.RestClient;
import java.util.Map;
/** Calls the Python analytics/AI service. Its outputs are ADVISORY only:
 *  the core never executes a clinical or financial decision from them
 *  without a human actor (Catalog v6 guardrail). Correlation ids propagate. */
@Service
public class AnalyticsClient {
  private final RestClient http;
  public AnalyticsClient(@Value("${app.analytics-ai-base-url}") String baseUrl) {
    this.http = RestClient.builder().baseUrl(baseUrl).build();
  }
  @SuppressWarnings("unchecked")
  public Map<String, Object> triageSupport(Map<String, Object> vitals, String correlationId) {
    return http.post().uri("/v1/ai/triage-support")
      .header("X-Correlation-Id", correlationId == null ? "" : correlationId)
      .contentType(MediaType.APPLICATION_JSON).body(vitals)
      .retrieve().body(Map.class);
  }
  @SuppressWarnings("unchecked")
  public Map<String, Object> capacityForecast(Map<String, Object> series, String correlationId) {
    return http.post().uri("/v1/forecast/capacity")
      .header("X-Correlation-Id", correlationId == null ? "" : correlationId)
      .contentType(MediaType.APPLICATION_JSON).body(series)
      .retrieve().body(Map.class);
  }
}
