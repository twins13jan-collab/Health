package eg.gov.health.platform.telemedicine;
/** Telemedicine rules (module 112). A remote session starts
 *  only when both parties are verified and consent is obtained
 *  — an unverified stranger on either end is not a consultation.
 *  It closes only with the limits of remote examination
 *  documented and the in-person decision recorded: remote care
 *  that pretends it has no limits is the hazard. Recording
 *  without authorization is prohibited, and tele-prescriptions
 *  follow the same regulations as any prescription. */
public final class TeleRules {
  private TeleRules() {}
  public static boolean startable(boolean partiesVerified, boolean consent) { return partiesVerified && consent; }
  public static boolean closable(boolean limitationsDocumented, String inPersonDecision) {
    return limitationsDocumented && inPersonDecision != null && !inPersonDecision.isBlank();
  }
}
