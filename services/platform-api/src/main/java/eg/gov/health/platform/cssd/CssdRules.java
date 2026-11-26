package eg.gov.health.platform.cssd;
import java.time.*;
/** CSSD rules (module 105). Reprocessing is a chain — receive, sort,
 *  clean, disinfect, dry, inspect, pack, sterilize — and a set is
 *  released only when EVERY step is documented and the chemical
 *  indicator passed. A load containing implants additionally needs a
 *  PASSED biological indicator before release; pending is not a pass.
 *  A failed indicator quarantines the load; a failure discovered
 *  after release triggers quarantine and recall of the affected sets.
 *  Sterile sets past their expiry are never issued. Every set used is
 *  linked to its procedure and patient for traceability. */
public final class CssdRules {
  private CssdRules() {}
  public static boolean chainComplete(boolean cleaning, boolean disinfection, boolean dried, boolean inspected, String packedBy) {
    return cleaning && disinfection && dried && inspected && packedBy != null && !packedBy.isBlank();
  }
  public static boolean releasable(boolean chainComplete, String chemical, String biological, boolean containsImplants) {
    if (!chainComplete) return false;
    if (!"pass".equals(chemical)) return false;
    if (containsImplants) return "pass".equals(biological); // implant loads wait for the biological result
    return !"fail".equals(biological);
  }
  public static boolean issuable(String setStatus, LocalDate expiry, LocalDate today) {
    return "sterile".equals(setStatus) && expiry != null && today != null && !today.isAfter(expiry);
  }
}
