package eg.gov.health.platform.chronicdeep;
/** Chronic-disease rules (module 97). Enrollment follows
 *  declared, published criteria — a programme list built on
 *  unpublished criteria is a favour list. Home measurements
 *  steer care only from verified sources: an unverified reading
 *  is recorded, flagged, and never counted toward control,
 *  because treating a number nobody vouched for is how control
 *  statistics learn to lie. */
public final class ChronicRules {
  private ChronicRules() {}
  public static boolean enrollable(String criteriaRef, boolean meetsCriteria) {
    return criteriaRef != null && !criteriaRef.isBlank() && meetsCriteria;
  }
  public static boolean countsTowardControl(boolean sourceVerified) { return sourceVerified; }
  public static boolean controlled(double latestVerifiedValue, double targetMax) {
    return latestVerifiedValue <= targetMax;
  }
}
