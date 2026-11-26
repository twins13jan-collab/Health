package eg.gov.health.platform.reports;
import java.math.*;
/** National reporting rules (cycle 236). Every indicator is recomputable:
 *  definition + formula + source + cutoff + coverage. A late facility
 *  shows up as a coverage percentage — its absence is never generalised
 *  into a national figure. A definition change issues a NEW version, and
 *  comparing across versions without a warning is forbidden. Small,
 *  sensitive counts are suppressed or aggregated. Publication needs an
 *  approved definition, a validated job, de-identification, and the
 *  national authority's approval; the public sees published data only. */
public final class ReportRules {
  private ReportRules() {}
  public static final double NATIONAL_COVERAGE_THRESHOLD = 80.0;
  public static final long SMALL_COUNT_THRESHOLD = 5;
  public static double coveragePct(int participating, int expected) {
    if (expected <= 0) return 0.0;
    return Math.round(participating * 10000.0 / expected) / 100.0;
  }
  public static String dataClass(double coveragePct, boolean anyLate) {
    if (coveragePct >= 100.0) return "confirmed";
    if (coveragePct >= NATIONAL_COVERAGE_THRESHOLD) return anyLate ? "late" : "estimated";
    return "incomplete";
  }
  /** A figure below the coverage threshold must not be presented as national. */
  public static boolean nationalClaimAllowed(double coveragePct) {
    return coveragePct >= NATIONAL_COVERAGE_THRESHOLD;
  }
  public static boolean smallCountSuppressed(long numerator) {
    return numerator > 0 && numerator < SMALL_COUNT_THRESHOLD;
  }
  /** Cross-version comparison needs an explicit heterogeneity warning. */
  public static boolean comparabilityWarningRequired(int fromVersion, int toVersion) {
    return fromVersion != toVersion;
  }
  public static boolean publishable(String definitionStatus, String jobStatus, boolean deidentified, String approvedBy) {
    return "approved".equals(definitionStatus) && "validated".equals(jobStatus)
        && deidentified && approvedBy != null && !approvedBy.isBlank();
  }
  public static BigDecimal compute(BigDecimal numerator, BigDecimal denominator) {
    if (numerator == null || denominator == null || denominator.signum() == 0) return null;
    return numerator.divide(denominator, 4, RoundingMode.HALF_UP);
  }
}
