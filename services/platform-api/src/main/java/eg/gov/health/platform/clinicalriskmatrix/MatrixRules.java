package eg.gov.health.platform.clinicalriskmatrix;
/** Clinical & medication risk-matrix rules (module 178). Every
 *  matrix hazard — identity, medication, dose, procedure,
 *  critical results, delay, transport, handover, device outage
 *  — carries three control layers: prevention, detection and
 *  response. A hazard is "controlled" only when all three are
 *  documented; one missing layer means the hazard is open. And
 *  alerts are audited for fatigue: an alert that is wrong more
 *  than half the time, on a real sample, goes under review —
 *  an ignored alarm protects no one. */
public final class MatrixRules {
  private MatrixRules() {}
  public static final double FATIGUE_FP_RATE = 0.5;
  public static final int FATIGUE_MIN_SAMPLE = 20;
  public static int priority(Integer likelihood, Integer severity) {
    if (likelihood == null || severity == null) return 0;
    return likelihood * severity;
  }
  public static String band(int priority) {
    if (priority >= 15) return "extreme";
    if (priority >= 8) return "high";
    if (priority >= 4) return "medium";
    return "low";
  }
  public static boolean fullyControlled(String prevention, String detection, String response) {
    return prevention != null && !prevention.isBlank() && detection != null && !detection.isBlank()
      && response != null && !response.isBlank();
  }
  public static boolean alertFatigued(Integer totalFired, Integer falsePositives) {
    if (totalFired == null || falsePositives == null || totalFired < FATIGUE_MIN_SAMPLE) return false;
    return (falsePositives * 1.0 / totalFired) > FATIGUE_FP_RATE;
  }
}
