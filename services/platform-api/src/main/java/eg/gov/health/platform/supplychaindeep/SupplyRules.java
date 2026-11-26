package eg.gov.health.platform.supplychaindeep;
/** Supply-chain rules (module 68). A source supplies care only
 *  with regulatory approval. Dependence on one source at 80% or
 *  more of an item is a named high risk, and it counts as
 *  managed only when an alternative source is identified —
 *  a risk register that lists fragility without alternatives
 *  is a diary, not a defence. Emergency stock for critical
 *  items has a floor, and the shortfall is computed, not
 *  discovered during the crisis. */
public final class SupplyRules {
  private SupplyRules() {}
  public static final int SINGLE_SOURCE_THRESHOLD = 80;
  public static boolean highDependence(int maxSharePercent) { return maxSharePercent >= SINGLE_SOURCE_THRESHOLD; }
  public static boolean riskManaged(boolean highDependence, boolean alternativeIdentified) {
    return !highDependence || alternativeIdentified;
  }
  public static int emergencyShortfall(int floorUnits, int onHandUnits) {
    return Math.max(0, floorUnits - onHandUnits);
  }
}
