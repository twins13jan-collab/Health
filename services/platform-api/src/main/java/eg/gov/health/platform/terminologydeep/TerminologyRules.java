package eg.gov.health.platform.terminologydeep;
/** Terminology rules (module 137). A local term maps to a
 *  standard only when meaning preservation is verified and a
 *  specialist has reviewed it — an unverified mapping converts
 *  data by quietly losing its meaning, and the loss surfaces
 *  years later as wrong statistics. A term with no honest
 *  standard equivalent is documented as unsupported, never
 *  force-mapped to the nearest code. */
public final class TerminologyRules {
  private TerminologyRules() {}
  public static boolean usable(boolean supported, boolean meaningVerified, boolean specialistReviewed) {
    return supported && meaningVerified && specialistReviewed;
  }
}
