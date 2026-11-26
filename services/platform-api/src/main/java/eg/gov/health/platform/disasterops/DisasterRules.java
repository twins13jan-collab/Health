package eg.gov.health.platform.disasterops;
/** Disaster-operations rules (module 103). A multi-agency event
 *  is classified before plans activate — response scales to the
 *  classification, not to the noise. Resources move on logged
 *  decisions with an authority basis: a request is allocated by
 *  decision and delivered against that allocation, never ahead
 *  of it. And the event does not close while a resource request
 *  is still undelivered — somewhere, a facility is still waiting
 *  for what it was promised. */
public final class DisasterRules {
  private DisasterRules() {}
  public static boolean activationPermitted(Integer classification) {
    return classification != null && classification >= 1 && classification <= 3;
  }
  public static boolean deliveryPermitted(String requestStatus) { return "allocated".equals(requestStatus); }
  public static boolean eventClosable(long openRequests) { return openRequests == 0; }
}
