package eg.gov.health.platform.endoscopy;
/** Endoscopy rules (cycle 241). A scope is usable only when available
 *  AND its latest reprocessing cycle passed cleaning, disinfection and
 *  the leak test; a failed test quarantines the scope and its affected
 *  cases stay traceable. No procedure starts on an incomplete
 *  pre-procedure checklist. A specimen without a verified label is
 *  isolated and never attributed to a patient. Discharge follows a
 *  recovery (Aldrete) assessment. */
public final class EndoscopyRules {
  private EndoscopyRules() {}
  public static boolean reprocessingPassed(boolean cleaning, boolean disinfection, boolean leakTest) {
    return cleaning && disinfection && leakTest;
  }
  public static boolean scopeUsable(String scopeStatus, String latestReprocessingStatus) {
    return "available".equals(scopeStatus) && "passed".equals(latestReprocessingStatus);
  }
  public static boolean checklistComplete(boolean fasting, boolean medications, boolean allergy, boolean consent) {
    return fasting && medications && allergy && consent;
  }
  /** Aldrete >= 9 discharges; below needs a documented physician decision. */
  public static boolean dischargeable(Integer aldreteScore, String physicianDecision) {
    if (aldreteScore != null && aldreteScore >= 9) return true;
    return physicianDecision != null && !physicianDecision.isBlank();
  }
}
