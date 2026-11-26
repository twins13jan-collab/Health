package eg.gov.health.platform.vendormonitoring;
/** Vendor-monitoring rules (module 189). A vendor is at risk
 *  when its measured performance says so — on-time delivery
 *  under 90% or a quality score under 80 — computed from
 *  deliveries, not from relationships. And an exceptional
 *  purchase is legitimate only documented and reviewed after
 *  the fact: the emergency exception that never faces review
 *  becomes the normal channel, and procurement control dies
 *  quietly through it. */
public final class VendorRules {
  private VendorRules() {}
  public static boolean atRisk(double onTimePercent, double qualityScore) {
    return onTimePercent < 90.0 || qualityScore < 80.0;
  }
  public static boolean exceptionalLegitimate(boolean documented, boolean reviewed) {
    return documented && reviewed;
  }
}
