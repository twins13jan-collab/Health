package eg.gov.health.platform.auditcompliance;
/** Audit & compliance rules (module 118). An audit finding
 *  closes only with evidence, a corrective action, and
 *  verified closure — a finding closed on a promise reopens
 *  itself at the next incident. A fraud signal carries NO
 *  automatic judgment: it stays a signal until a human records
 *  an outcome, because an algorithm's suspicion ending a career
 *  or a payment without a human is abuse wearing a compliance
 *  badge. And a conflict of interest is disclosed and managed
 *  before the person acts on related decisions. */
public final class ComplianceRules {
  private ComplianceRules() {}
  public static boolean findingClosable(String evidenceRef, String correctiveAction, boolean verifiedClosed) {
    return evidenceRef != null && !evidenceRef.isBlank()
      && correctiveAction != null && !correctiveAction.isBlank() && verifiedClosed;
  }
  public static String signalStatus(boolean humanReviewed, String humanOutcome) {
    return humanReviewed && humanOutcome != null && !humanOutcome.isBlank() ? "human_decided" : "signal_only";
  }
}
