package eg.gov.health.platform.institutionalrisk;
/** Institutional-risk rules (module 134). A risk is scored
 *  before controls (inherent) and after them (residual) — the
 *  register shows both, because a control that exists only on
 *  paper changes nothing. A risk closes only with a named
 *  owner, a treatment plan, every treatment done, and a
 *  residual score below extreme — or a formal acceptance of
 *  the extreme residual by a named authority, on the record. */
public final class InstitutionalRiskRules {
  private InstitutionalRiskRules() {}
  public static final int EXTREME = 15;
  public static int score(Integer likelihood, Integer impact) {
    if (likelihood == null || impact == null) return 0;
    return likelihood * impact;
  }
  public static boolean closable(String ownerRef, String treatmentPlan, boolean allTreatmentsDone,
                                 Integer residualL, Integer residualI, String acceptedBy) {
    if (ownerRef == null || ownerRef.isBlank()) return false;
    if (treatmentPlan == null || treatmentPlan.isBlank()) return false;
    if (!allTreatmentsDone) return false;
    if (residualL == null || residualI == null) return false;
    if (score(residualL, residualI) >= EXTREME) return acceptedBy != null && !acceptedBy.isBlank();
    return true;
  }
}
