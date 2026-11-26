package eg.gov.health.platform.clinicalcosting;
/** Clinical-costing rules (module 165). Cost follows ACTUAL
 *  consumption: a resource planned but never executed costs
 *  nothing in this ledger, because the pathway cost must mirror
 *  what the patient actually received. Variance against the
 *  standard beyond 25% opens a review. And the value guard:
 *  costing informs decisions, it never makes them — a variance
 *  review is closed by a clinician, never by finance alone, so
 *  the medical decision is never reduced to its price. */
public final class CostingRules {
  private CostingRules() {}
  public static final double VARIANCE_THRESHOLD_PCT = 25.0;
  public static double variancePct(double actualTotal, Double standardCost) {
    if (standardCost == null || standardCost == 0) return 0.0;
    return Math.round(1000.0 * (actualTotal - standardCost) / standardCost) / 10.0;
  }
  public static boolean varianceReviewNeeded(double actualTotal, Double standardCost) {
    return Math.abs(variancePct(actualTotal, standardCost)) > VARIANCE_THRESHOLD_PCT;
  }
  public static boolean mayCloseVarianceReview(String role) {
    return role != null && (role.contains("clinical") || role.contains("physician") || role.contains("medical") || role.equals("admin"));
  }
}
