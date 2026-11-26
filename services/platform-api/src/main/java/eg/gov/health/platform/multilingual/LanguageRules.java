package eg.gov.health.platform.multilingual;
/** Language & usability rules (module 126). A medical
 *  translation is published only after medical review — a
 *  machine-translated dosage instruction is a clinical hazard
 *  wearing a language badge. And an accessibility claim stands
 *  only when the check passed AND the surface was tested with
 *  diverse users: a checklist ticked by the developer alone
 *  certifies the developer's imagination, not the patient's
 *  access. */
public final class LanguageRules {
  private LanguageRules() {}
  public static boolean publishable(boolean medicalReviewed) { return medicalReviewed; }
  public static boolean accessibilityClaimValid(boolean passed, boolean testedWithDiverseUsers) {
    return passed && testedWithDiverseUsers;
  }
}
