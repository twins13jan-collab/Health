package eg.gov.health.platform.facilityreadiness;
import java.util.*;
/** Facility-readiness rules (module 190). A service is
 *  activated only when all eight readiness checks hold —
 *  licences, staff, equipment, stock, infrastructure,
 *  protocols, training and referral paths — and a named
 *  authorized person takes the decision. A "not ready" verdict
 *  is equally disciplined: every blocker carries a remediation
 *  plan, because a blocker without a plan is a complaint, not
 *  management. Readiness is re-verified whenever capability
 *  changes or a failure occurs, and its state, limits and
 *  source are shown. */
public final class FacilityReadinessRules {
  private FacilityReadinessRules() {}
  public static List<String> blockers(boolean licences, boolean staff, boolean equipment, boolean stock,
                                      boolean infrastructure, boolean protocols, boolean training, boolean referralPaths) {
    List<String> b = new ArrayList<>();
    if (!licences) b.add("licences");
    if (!staff) b.add("staff");
    if (!equipment) b.add("equipment");
    if (!stock) b.add("stock");
    if (!infrastructure) b.add("infrastructure");
    if (!protocols) b.add("protocols");
    if (!training) b.add("training");
    if (!referralPaths) b.add("referral_paths");
    return b;
  }
  public static boolean activatable(List<String> blockers, String decidedBy) {
    return blockers.isEmpty() && decidedBy != null && !decidedBy.isBlank();
  }
  public static boolean notReadyRecordable(List<String> blockers, String remediationPlan) {
    return !blockers.isEmpty() && remediationPlan != null && !remediationPlan.isBlank();
  }
}
