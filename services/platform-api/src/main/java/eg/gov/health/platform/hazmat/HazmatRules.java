package eg.gov.health.platform.hazmat;
/** Hazmat rules (module 173). Contamination is contained before
 *  it is treated: a contaminated casualty does not enter the
 *  treatment zone until decontaminated — one un-decontaminated
 *  patient can close an emergency department. Zones are
 *  established, specialist coordination (poison centre, civil
 *  defence, radiation authority) is made for chemical and
 *  radiological events, and the incident does not close while
 *  any exposed person still owes a follow-up examination. */
public final class HazmatRules {
  private HazmatRules() {}
  public static boolean treatmentEntryPermitted(boolean contaminated, boolean decontaminated) {
    return !contaminated || decontaminated;
  }
  public static boolean specialistCoordinationRequired(String agentType) {
    return "chemical".equals(agentType) || "radiological".equals(agentType);
  }
  public static boolean incidentClosable(boolean zonesEstablished, boolean coordinationRequired, boolean coordinationDone,
                                         long exposuresOwingFollowUp) {
    return zonesEstablished && (!coordinationRequired || coordinationDone) && exposuresOwingFollowUp == 0;
  }
}
