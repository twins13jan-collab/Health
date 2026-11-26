package eg.gov.health.platform.clinicalquality;
/** Clinical-quality rules (module 55). Outcome indicators are
 *  compared only case-mix adjusted — a raw mortality rate
 *  across different case mixes punishes the hospital that takes
 *  the hardest cases. A death enters quality closure only after
 *  mortality review. And an improvement plan closes only when
 *  its effectiveness is verified: actions completed is effort,
 *  not improvement. */
public final class QualityRules {
  private QualityRules() {}
  public static boolean comparable(boolean caseMixAdjusted) { return caseMixAdjusted; }
  public static boolean planClosable(int actionsTotal, int actionsDone, boolean effectivenessVerified) {
    return actionsTotal > 0 && actionsDone >= actionsTotal && effectivenessVerified;
  }
}
