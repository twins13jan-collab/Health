package eg.gov.health.platform.patientsafety;
/** Patient-safety rules (module 156). Reporting is confidential
 *  and the reporter protected — an identity exposed is a silence
 *  purchased. A patient harmed is told, per policy and law, and
 *  the incident does not close until they have been. Learning
 *  closes the loop: systemic and human-factor analysis done,
 *  and every prevention action completed — an analysis without
 *  prevention is an obituary, not a lesson. */
public final class PatientSafetyRules {
  private PatientSafetyRules() {}
  public static boolean closable(String incidentType, boolean patientNotified, boolean analyzed, boolean allPreventionDone) {
    if ("harm".equals(incidentType) && !patientNotified) return false;
    return analyzed && allPreventionDone;
  }
}
