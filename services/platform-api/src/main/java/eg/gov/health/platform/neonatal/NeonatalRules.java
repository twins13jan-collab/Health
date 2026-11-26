package eg.gov.health.platform.neonatal;
/** Newborn & NICU rules (cycle 245 + module 48). Hard rules: an invalid
 *  sample is never a normal result — recollection is mandatory; an abnormal
 *  result opens a referral that closes only with a recorded outcome; twins
 *  and multiples require double identity verification for every procedure;
 *  identity links to the mother by identifiers, never by name alone. */
public final class NeonatalRules {
  private NeonatalRules() {}
  /** APGAR bands: 7-10 reassuring, 4-6 moderately abnormal, 0-3 low. */
  public static String apgarBand(int score) {
    if (score < 0 || score > 10) throw new IllegalArgumentException("APGAR out of range");
    if (score >= 7) return "reassuring";
    if (score >= 4) return "moderately_abnormal";
    return "low_critical";
  }
  public static boolean apgarNeedsEscalation(int score) { return apgarBand(score).equals("low_critical"); }
  /** Birth-weight classes (grams). */
  public static String birthWeightClass(int grams) {
    if (grams <= 0) throw new IllegalArgumentException("weight must be positive");
    if (grams < 1000) return "elbw";
    if (grams < 1500) return "vlbw";
    if (grams < 2500) return "lbw";
    if (grams >= 4000) return "macrosomia";
    return "normal";
  }
  public static boolean isLowBirthWeight(int grams) { return grams < 2500; }
  /** Physiological weight loss in the first days: >10% of birth weight alerts. */
  public static double weightLossPct(int birthGrams, int currentGrams) {
    if (birthGrams <= 0) throw new IllegalArgumentException("birth weight required");
    return Math.round((birthGrams - currentGrams) * 1000.0 / birthGrams) / 10.0;
  }
  public static boolean excessiveWeightLoss(int birthGrams, int currentGrams) { return weightLossPct(birthGrams, currentGrams) > 10.0; }
  /** Screening result handling: what the workflow must do with a result. */
  public record ScreeningOutcome(String result, boolean recollectRequired, boolean referralOpens, boolean requiresAcknowledgment) {}
  public static ScreeningOutcome onResult(String result) {
    return switch (result) {
      case "normal" -> new ScreeningOutcome("normal", false, false, false);
      // An invalid sample is NOT a normal result: recollect, keep tracking.
      case "invalid" -> new ScreeningOutcome("invalid", true, false, false);
      // Abnormal: notify + acknowledgment + referral opens until closed.
      case "abnormal" -> new ScreeningOutcome("abnormal", false, true, true);
      default -> throw new IllegalArgumentException("unknown screening result: " + result);
    };
  }
  /** Referral closure requires a recorded outcome. */
  public static boolean referralClosable(String referralStatus, String outcome) {
    return "open".equals(referralStatus) && outcome != null && !outcome.isBlank();
  }
  /** Twins/multiples: double verification for every procedure. */
  public static boolean doubleVerificationRequired(int siblingCount) { return siblingCount > 1; }
}
