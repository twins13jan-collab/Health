package eg.gov.health.platform.healthequity;
/** Equity rules (module 74). Distribution is measured against
 *  population AND need: service units per 10,000 people,
 *  adjusted by the disease-burden index — a region with twice
 *  the burden needs twice the service to be equally served.
 *  Gap analyses publish only on privacy-safe samples; small
 *  groups are suppressed, never exposed. A coverage programme
 *  closes only when a fresh measurement shows the gap at or
 *  below its target. */
public final class EquityRules {
  private EquityRules() {}
  public static final double UNDERSERVED_THRESHOLD = 1.0; // need-adjusted units per 10k
  public static final int MIN_PUBLISHABLE_SAMPLE = 30;
  public static double needAdjustedUnitsPer10k(Integer units, Integer population, Double burdenIndex) {
    if (units == null || population == null || population == 0 || burdenIndex == null || burdenIndex <= 0) return 0.0;
    double raw = units * 10000.0 / population;
    return Math.round(100.0 * raw / burdenIndex) / 100.0;
  }
  public static boolean underserved(double needAdjusted) { return needAdjusted < UNDERSERVED_THRESHOLD; }
  public static boolean gapPublishable(Integer sampleSize) { return sampleSize != null && sampleSize >= MIN_PUBLISHABLE_SAMPLE; }
  public static boolean programClosable(Double latestGapPct, Double targetGapPct) {
    return latestGapPct != null && targetGapPct != null && latestGapPct <= targetGapPct;
  }
}
