package eg.gov.health.platform.meddelivery;
/** Medication-delivery rules (module 152). A home delivery starts
 *  only on a verified delivery-eligible prescription, reviewed and
 *  approved by a pharmacist, and prepared before dispatch. Special-
 *  handling medication (controlled or cold-chain) is handed over
 *  only against verified identity, and a cold-chain breach voids
 *  the delivery — the pack returns, it is never left "probably
 *  fine". Two failed attempts open the return workflow, and a
 *  returned temperature-sensitive pack is quarantined, never
 *  restocked. */
public final class DeliveryRules {
  private DeliveryRules() {}
  public static boolean dispatchPermitted(boolean eligibilityVerified, boolean pharmacistApproved, String status) {
    return eligibilityVerified && pharmacistApproved && "prepared".equals(status);
  }
  public static boolean handoverValid(boolean specialHandling, boolean identityVerified, Boolean temperatureOk, boolean coldChainRequired) {
    if (specialHandling && !identityVerified) return false;
    if (coldChainRequired && !Boolean.TRUE.equals(temperatureOk)) return false;
    return true;
  }
  public static boolean returnRequired(long failedAttempts) { return failedAttempts >= 2; }
  public static String returnDisposition(boolean coldChainRequired, boolean temperatureOk) {
    return (coldChainRequired && !temperatureOk) ? "quarantine" : "restock";
  }
}
