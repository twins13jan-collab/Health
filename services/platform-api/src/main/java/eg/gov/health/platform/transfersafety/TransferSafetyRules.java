package eg.gov.health.platform.transfersafety;
/** Transfer-safety rules (module 186). An interfacility transfer
 *  departs only when three confirmations exist: the patient is
 *  assessed stable for the journey (or the risk is accepted with
 *  the right escort), the receiving facility has ACCEPTED the
 *  patient, and its capacity is confirmed. An unstable patient
 *  travels with a physician or ICU team — never with a note. And
 *  the transfer completes at documented handover on arrival, not
 *  at the sending door. */
public final class TransferSafetyRules {
  private TransferSafetyRules() {}
  public static boolean departurePermitted(boolean stabilityConfirmed, boolean acceptanceConfirmed, boolean capacityConfirmed) {
    return stabilityConfirmed && acceptanceConfirmed && capacityConfirmed;
  }
  public static boolean escortAdequate(Boolean patientStable, String escortLevel) {
    if (Boolean.TRUE.equals(patientStable)) return true;
    return "physician".equals(escortLevel) || "icu_team".equals(escortLevel);
  }
}
