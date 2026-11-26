package eg.gov.health.platform.radiologydeep;
/** Radiology rules (module 93). Contrast is a kidney decision
 *  before it is an image decision: no contrast exam proceeds
 *  without contraindications checked AND a recorded eGFR, and
 *  below eGFR 30 contrast is blocked unless a nephrology
 *  override is documented. A final report is never edited — a
 *  correction is a new report that supersedes it. And a
 *  critical finding is not communicated until a named receiver
 *  acknowledges it. */
public final class RadiologyRules {
  private RadiologyRules() {}
  public static boolean contrastPermitted(boolean contraindicationsChecked, boolean contrastPlanned,
                                          Double egfr, boolean nephrologyOverride) {
    if (!contrastPlanned) return true;
    if (!contraindicationsChecked || egfr == null) return false;
    if (egfr < 30) return nephrologyOverride;
    return true;
  }
  public static boolean orderClosable(boolean hasCriticalResult, boolean criticalAcknowledged) {
    return !hasCriticalResult || criticalAcknowledged;
  }
}
