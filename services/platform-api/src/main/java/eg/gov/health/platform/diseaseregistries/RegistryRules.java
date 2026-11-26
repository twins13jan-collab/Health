package eg.gov.health.platform.diseaseregistries;
/** Registry rules (module 174). A registry is only as honest as
 *  its case definition and its follow-up: cases enter only when
 *  they MEET the unified definition, and aggregate results
 *  publish only as aggregates (cohort >= 30) with the
 *  lost-to-follow-up rate computed and shown — above 30% loss,
 *  a bias note is mandatory, because silently publishing
 *  survivorship is how registries lie. */
public final class RegistryRules {
  private RegistryRules() {}
  public static final int MIN_COHORT = 30;
  public static final double BIAS_NOTE_ABOVE_LOSS_PCT = 30.0;
  public static double lostRatePct(long total, long lost) {
    if (total == 0) return 0.0;
    return Math.round(1000.0 * lost / total) / 10.0;
  }
  public static boolean publishable(long totalCases, double lostRatePct, String biasNote) {
    if (totalCases < MIN_COHORT) return false;
    return lostRatePct <= BIAS_NOTE_ABOVE_LOSS_PCT || (biasNote != null && !biasNote.isBlank());
  }
}
