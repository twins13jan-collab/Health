package eg.gov.health.platform.socialservices;
/** Social-services rules (module 99). A person in crisis gets a
 *  safety plan before anything else on the list moves. A
 *  safeguarding concern — a child or a vulnerable adult — is not
 *  a note in a file: it mandates referral to the protection
 *  authority, and the support plan cannot close while that
 *  referral is unmade. Social records are among the most
 *  sensitive in the building and stay behind role walls. */
public final class SocialRules {
  private SocialRules() {}
  public static boolean safetyPlanRequired(String riskLevel) { return "crisis".equals(riskLevel) || "high".equals(riskLevel); }
  public static boolean safeguardingReferralRequired(String concern) { return "child".equals(concern) || "vulnerable_adult".equals(concern); }
  public static boolean planClosable(String riskLevel, boolean safetyPlanCreated, String concern, boolean referralMade) {
    if (safetyPlanRequired(riskLevel) && !safetyPlanCreated) return false;
    if (safeguardingReferralRequired(concern) && !referralMade) return false;
    return true;
  }
}
