package eg.gov.health.platform.facilitiesinfra;
/** Infrastructure rules (module 108). A critical system —
 *  power, medical gas, cooling for the blood bank — that is
 *  down is a clinical event, not a maintenance ticket: its work
 *  order cannot close without a fallback plan in place AND a
 *  post-repair reading back within limits. Paper repairs do not
 *  restore oxygen. */
public final class InfraRules {
  private InfraRules() {}
  public static boolean workOrderClosable(boolean systemCritical, String criticality, String fallbackPlan, boolean latestReadingWithinLimits) {
    boolean criticalContext = systemCritical || "critical".equals(criticality);
    if (!criticalContext) return true;
    return fallbackPlan != null && !fallbackPlan.isBlank() && latestReadingWithinLimits;
  }
}
