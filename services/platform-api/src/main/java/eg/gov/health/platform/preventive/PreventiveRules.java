package eg.gov.health.platform.preventive;
/** Preventive-screening rules (module 151). Screening is offered
 *  to the eligible, with consent, at a booked appointment — and a
 *  positive SCREENING result is not a diagnosis: it opens a
 *  confirmatory pathway, and the pathway closes only when the
 *  confirmatory test is done and the referral is created. People
 *  who do not show are followed up and re-invited; silence is not
 *  a screening outcome. Coverage is measured honestly: attended
 *  over invited. */
public final class PreventiveRules {
  private PreventiveRules() {}
  public static boolean screeningPermitted(boolean eligible, boolean consentGiven) { return eligible && consentGiven; }
  public static boolean pathwayClosable(String result, boolean confirmatoryDone, boolean referralCreated) {
    if ("negative".equals(result)) return true;
    return confirmatoryDone && referralCreated; // positive screens close only through confirmation + referral
  }
  public static double coverage(long attended, long invited) {
    return invited == 0 ? 0.0 : Math.round(1000.0 * attended / invited) / 10.0;
  }
}
