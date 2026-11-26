package eg.gov.health.platform.criticalcare;
import java.util.*;
/** Critical-care transfer rules (module 153). A critical
 *  patient changes care level only when all four hold: the
 *  level criteria are met, stability has been reviewed, the
 *  transfer team and equipment fit the patient, and the
 *  handover is documented. A free bed is a logistics fact, not
 *  a clinical decision — the sickest patient in the building
 *  never moves on logistics alone. */
public final class CriticalCareRules {
  private CriticalCareRules() {}
  public static List<String> transferBlockers(boolean criteriaMet, boolean stabilityReviewed, boolean teamEquipped, boolean handoverDocumented) {
    List<String> b = new ArrayList<>();
    if (!criteriaMet) b.add("criteria_unmet");
    if (!stabilityReviewed) b.add("stability_unreviewed");
    if (!teamEquipped) b.add("team_or_equipment_unfit");
    if (!handoverDocumented) b.add("handover_undocumented");
    return b;
  }
}
