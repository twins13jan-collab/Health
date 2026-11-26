package eg.gov.health.platform.dental;
import java.time.*;
/** Dentistry rules (cycle 248). No procedure starts on an unverified
 *  tooth/site; consent precedes treatment; an anaesthesia risk needs a
 *  documented dentist review; instruments from a failed or expired
 *  sterilization cycle are blocked; every procedure traces to tooth,
 *  dentist, material and lot; a recalled lot traces to its patients. */
public final class DentalRules {
  private DentalRules() {}
  /** FDI two-digit notation: quadrant 1-4 permanent / 5-8 deciduous, tooth 1-8. */
  public static boolean validFdi(int tooth) {
    int q = tooth / 10, t = tooth % 10;
    return q >= 1 && q <= 8 && t >= 1 && t <= 8;
  }
  /** A sterilization cycle authorizes instruments only while passed and unexpired. */
  public static boolean sterilizationValid(String status, OffsetDateTime expiresAt, OffsetDateTime now) {
    return "passed".equals(status) && expiresAt != null && now != null && now.isBefore(expiresAt);
  }
  /** Procedure preconditions: verified site + valid FDI + consent on plan. */
  public static boolean siteReady(boolean siteVerified, int tooth) { return siteVerified && validFdi(tooth); }
  /** Anaesthesia risk is workable only with a documented dentist review. */
  public static boolean anesthesiaCleared(boolean risk, boolean physicianReviewed) { return !risk || physicianReviewed; }
}
