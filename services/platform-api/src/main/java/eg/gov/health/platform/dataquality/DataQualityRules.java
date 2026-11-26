package eg.gov.health.platform.dataquality;
/** Data-quality rules (module 122). Every number the system
 *  publishes carries its confidence: completeness, validity and
 *  consistency as measured, freshness decaying with age. Below
 *  70 the figure is low-confidence and may publish only WITH
 *  its caveats attached — a precise-looking number on rotten
 *  data is worse than no number. Corrections of conflicting
 *  records state which source of truth decided them. */
public final class DataQualityRules {
  private DataQualityRules() {}
  public static final double LOW_CONFIDENCE_BELOW = 70.0;
  public static double confidence(Double completeness, Double validity, Double consistency, Double freshnessHours) {
    double c = completeness == null ? 0 : completeness, v = validity == null ? 0 : validity, s = consistency == null ? 0 : consistency;
    double fresh = freshnessHours == null ? 0 : Math.max(0, 100 - freshnessHours * 2); // stale data decays 2 pts/hour
    return Math.round(10.0 * (0.30 * c + 0.30 * v + 0.25 * s + 0.15 * fresh)) / 10.0;
  }
  public static boolean publishable(double confidence, String caveat) {
    return confidence >= LOW_CONFIDENCE_BELOW || (caveat != null && !caveat.isBlank());
  }
}
