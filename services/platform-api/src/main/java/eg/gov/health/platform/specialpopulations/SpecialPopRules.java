package eg.gov.health.platform.specialpopulations;
/** Special-populations rules (module 70). Care in detention is
 *  healthcare under legal safeguards: the safeguards are
 *  confirmed and the patient's rights are explained before the
 *  record closes — custody changes the setting, never the
 *  standard. Refugees, displaced and mobile people meet no
 *  unjustified barriers: when care is not provided and the
 *  recorded barrier is documents or fees, the encounter is
 *  flagged for denial review automatically. A detention service
 *  point activates only with verified safeguards. */
public final class SpecialPopRules {
  private SpecialPopRules() {}
  public static boolean pointActivatable(String siteType, boolean safeguardsVerified) {
    return !"detention".equals(siteType) || safeguardsVerified;
  }
  public static boolean detentionRecordClosable(boolean safeguardsConfirmed, boolean rightsInformed) {
    return safeguardsConfirmed && rightsInformed;
  }
  public static boolean denialReviewRequired(String barrierFlag, boolean careProvided) {
    return !careProvided && ("documents".equals(barrierFlag) || "fees".equals(barrierFlag));
  }
}
