package eg.gov.health.platform.clinicaltrials;
/** Clinical-trial rules (module 130). A trial recruits only after
 *  ethics AND regulatory approval with conflicts of interest
 *  declared. Participants are enrolled under a code — research data
 *  stays separated from care data — and only after eligibility is
 *  confirmed and informed consent documented; no visit or sample
 *  precedes consent. Withdrawal is immediate and final for new
 *  research activity. A serious adverse event that is not reported
 *  to the ethics committee is a breach, surfaced on the record. */
public final class TrialRules {
  private TrialRules() {}
  public static boolean recruitmentPermitted(boolean ethicsApproved, boolean regulatoryApproved, boolean coiDeclared) {
    return ethicsApproved && regulatoryApproved && coiDeclared;
  }
  public static boolean enrollmentPermitted(String trialStatus, boolean eligibilityConfirmed, boolean consentDocumented) {
    return "recruiting".equals(trialStatus) && eligibilityConfirmed && consentDocumented;
  }
  public static boolean researchActivityPermitted(String consentStatus) {
    return "consented".equals(consentStatus);
  }
  public static boolean saeBreach(String severity, boolean reportedToEthics) {
    return "serious".equals(severity) && !reportedToEthics;
  }
}
