package eg.gov.health.platform.observability;
/** Observability rules (module 62). A metric over its threshold
 *  is a breach, surfaced — never averaged away. An operational
 *  incident closes only with a recorded resolution, and a
 *  CRITICAL incident must pass through escalation before it can
 *  close: severity buys attention before it buys closure. */
public final class ObservabilityRules {
  private ObservabilityRules() {}
  public static boolean breached(Double value, Double threshold) {
    return value != null && threshold != null && value > threshold;
  }
  public static boolean closable(String severity, String status, String resolutionNote) {
    if (resolutionNote == null || resolutionNote.isBlank()) return false;
    if ("critical".equals(severity)) return "escalated".equals(status);
    return true;
  }
}
