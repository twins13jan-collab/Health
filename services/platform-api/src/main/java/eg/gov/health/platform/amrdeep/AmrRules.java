package eg.gov.health.platform.amrdeep;
/** AMR rules (module 167). A resistance rate is computed only
 *  from standardized-method results — mixing methods manufactures
 *  trends that do not exist. An unusual pattern (a jump of more
 *  than 20 points over baseline) must be verified before it is
 *  published: an artifact announced as an outbreak spends real
 *  resources and real trust. And surveillance reports are
 *  identity-protected aggregates, always. */
public final class AmrRules {
  private AmrRules() {}
  public static final double UNUSUAL_JUMP_POINTS = 20.0;
  public static double resistanceRate(long resistant, long totalStandardized) {
    if (totalStandardized <= 0) return -1;
    return Math.round(resistant * 1000.0 / totalStandardized) / 10.0;
  }
  public static boolean unusualPattern(double rate, Double baseline) {
    return baseline != null && rate - baseline > UNUSUAL_JUMP_POINTS;
  }
  public static boolean publishable(boolean identityProtected, boolean unusual, boolean patternVerified) {
    return identityProtected && (!unusual || patternVerified);
  }
}
