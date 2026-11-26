package eg.gov.health.platform.patientrights;
/** Complaint rules (cycle 232). Reception registers a complaint and
 *  never alters its content — corrections live in the timeline, not in
 *  the original text. The investigator sees the minimum necessary and
 *  every access is logged. Imminent risk escalates to the safety path
 *  immediately. A declared conflict of interest reassigns the
 *  investigation. Governance separates investigation from decision.
 *  Closure requires a resolution AND a root cause linked to a
 *  corrective action. Anonymous complaints are kept for analysis
 *  without inventing an identity. Third-party data disclosure passes
 *  privacy AND legal review first. */
public final class ComplaintRules {
  private ComplaintRules() {}
  public static boolean deciderIndependent(String investigator, String decider) {
    return decider != null && !decider.isBlank() && (investigator == null || !decider.equals(investigator));
  }
  public static boolean closable(String resolutionDecision, String rootCause, String correctiveAction) {
    return resolutionDecision != null && !resolutionDecision.isBlank()
        && rootCause != null && !rootCause.isBlank()
        && correctiveAction != null && !correctiveAction.isBlank();
  }
  public static boolean disclosureApprovable(String privacyReview, String legalReview) {
    return "approved".equals(privacyReview) && "approved".equals(legalReview);
  }
  public static boolean anonymousValid(boolean anonymous, String complainantName) {
    // Anonymous complaints are stored for analysis; no identity is invented.
    return anonymous || (complainantName != null && !complainantName.isBlank());
  }
}
