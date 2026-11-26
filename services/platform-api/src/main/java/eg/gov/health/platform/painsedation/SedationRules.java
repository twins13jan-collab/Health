package eg.gov.health.platform.painsedation;
/** Pain & procedural-sedation rules (module 86). Pain is measured
 *  with a scale that fits the patient: FLACC for the preverbal
 *  child, faces scales for children, numeric scales for adults —
 *  the wrong scale is a wrong measurement. Procedural sedation
 *  outside the operating theatre starts only on a documented
 *  pre-procedure assessment, an assessed airway and documented
 *  consent. Discharge after sedation is earned against recovery
 *  criteria: an Aldrete score below 9 is a patient who stays
 *  monitored, not a patient who goes home. */
public final class SedationRules {
  private SedationRules() {}
  public static final int MIN_DISCHARGE_ALDRETE = 9;
  public static boolean scaleAppropriate(String scale, String ageGroup) {
    return switch (ageGroup == null ? "" : ageGroup) {
      case "preverbal" -> "FLACC".equals(scale);
      case "child" -> "FLACC".equals(scale) || "WONGBAKER".equals(scale);
      case "adult" -> "NRS".equals(scale) || "VAS".equals(scale);
      default -> false;
    };
  }
  public static boolean sedationStartPermitted(boolean preAssessment, boolean airwayAssessed, boolean consentDocumented) {
    return preAssessment && airwayAssessed && consentDocumented;
  }
  public static boolean dischargeSafe(Integer aldreteScore) {
    return aldreteScore != null && aldreteScore >= MIN_DISCHARGE_ALDRETE;
  }
}
