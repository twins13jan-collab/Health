package eg.gov.health.platform.proms;
/** PROMs/PREMs rules (module 170). Only validated instruments —
 *  approved versions in an approved language version — are ever
 *  scheduled, at defined points in the care pathway. A response
 *  carrying an urgent-need signal is a clinical event, not survey
 *  data: it must be followed up before the schedule closes.
 *  Published analysis respects its own limits: below a minimum
 *  sample the figures describe the respondents, not the service,
 *  and are not published as conclusions. PROMs complement the
 *  clinical assessment; they never replace it. */
public final class PromRules {
  private PromRules() {}
  public static final int MIN_PUBLISHABLE_SAMPLE = 30;
  public static boolean instrumentUsable(boolean validated) { return validated; }
  public static boolean scheduleClosable(boolean urgentSignal, boolean urgentFollowedUp) {
    return !urgentSignal || urgentFollowedUp;
  }
  public static boolean analysisPublishable(int sampleSize, Double responseRatePct) {
    return sampleSize >= MIN_PUBLISHABLE_SAMPLE && responseRatePct != null && responseRatePct > 0;
  }
}
