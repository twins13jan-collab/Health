package eg.gov.health.platform.rehab;
/** Rehabilitation rules (cycle 246). Proposed goals and measured outcomes
 *  are kept strictly separate: the number of sessions alone NEVER proves
 *  improvement — only a measured outcome against the baseline does.
 *  Acute pain or deterioration stops the intervention and escalates. */
public final class RehabRules {
  private RehabRules() {}
  /** Sessions may start only with a baseline assessment AND an approved plan. */
  public static boolean sessionsAllowed(boolean baselineExists, String planStatus) {
    return baselineExists && ("approved".equals(planStatus) || "active".equals(planStatus));
  }
  /** Acute pain (>=8/10) or a deterioration incident stops the session. */
  public static boolean mustStopAndEscalate(Integer painDuring, String incident) {
    if (painDuring != null && painDuring >= 8) return true;
    return "deterioration".equals(incident) || "fall".equals(incident);
  }
  /** Measured change between two scores of the same instrument. */
  public static double delta(double baseline, double current) {
    return Math.round((current - baseline) * 10.0) / 10.0;
  }
  /** Goal met = measured current reaches the target (direction-aware). */
  public static boolean goalMet(double baseline, double target, double current, boolean higherIsBetter) {
    if (higherIsBetter) return current >= target && current > baseline;
    return current <= target && current < baseline;
  }
  /** Progress verdict: improvement is a MEASURED claim, never a session count. */
  public static String progressVerdict(Double baseline, Double latest, Integer targetAdl, int sessionCount) {
    if (baseline == null || latest == null) return "insufficient_measurement";
    if (targetAdl != null && goalMet(baseline, targetAdl, latest, true)) return "goal_met";
    if (delta(baseline, latest) > 0) return "improving_not_yet_goal";
    if (delta(baseline, latest) < 0) return "declining_review_plan";
    return "no_measured_change_review_plan";
  }
  /** A missed session is a documentation event: a reason is mandatory. */
  public static boolean missedReasonValid(String reason) { return reason != null && !reason.isBlank(); }
  /** An outcome measure is valid only with instrument AND version. */
  public static boolean outcomeValid(String instrument, String version) {
    return instrument != null && !instrument.isBlank() && version != null && !version.isBlank();
  }
}
