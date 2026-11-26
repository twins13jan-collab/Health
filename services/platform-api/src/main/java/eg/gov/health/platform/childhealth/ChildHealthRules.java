package eg.gov.health.platform.childhealth;
/** Child-health rules (module 96). A dose from a broken
 *  cold chain is not a dose — it is recorded invalid and must
 *  be repeated, because counting it would vaccinate the record
 *  instead of the child. And an adverse event following
 *  immunization must be investigated before the same vaccine
 *  is given to that child again. */
public final class ChildHealthRules {
  private ChildHealthRules() {}
  public static boolean doseValid(boolean coldChainOk) { return coldChainOk; }
  public static boolean nextDosePermitted(boolean aefiForSameVaccine, boolean aefiInvestigated) {
    return !aefiForSameVaccine || aefiInvestigated;
  }
}
