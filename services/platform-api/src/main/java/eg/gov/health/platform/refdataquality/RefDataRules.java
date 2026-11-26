package eg.gov.health.platform.refdataquality;
/** Reference-data rules (module 182). A sensitive code match —
 *  a diagnosis, a cause of death — activates only after human
 *  review: automated matching at scale is how a wrong code
 *  becomes the official truth in ten thousand records at once.
 *  Unknown or incompatible values are flagged and counted,
 *  never silently dropped, because a dropped value is a patient
 *  or a facility that quietly stops existing in the statistics.
 *  And every change to a mapping keeps its history. */
public final class RefDataRules {
  private RefDataRules() {}
  public static boolean activatable(boolean sensitive, boolean humanReviewed) {
    return !sensitive || humanReviewed;
  }
}
