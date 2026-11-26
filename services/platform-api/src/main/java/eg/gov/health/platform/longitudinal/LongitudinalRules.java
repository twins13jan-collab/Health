package eg.gov.health.platform.longitudinal;
import java.util.Set;
/** Longitudinal record & identity rules (cycle 217). Matching is
 *  explainable and human-reviewed for high-risk cases: a registrar
 *  creates local identities but never finalises a high-risk merge
 *  alone — the identity quality officer decides, with dual approval
 *  (a second, different approver) when the match is uncertain or a
 *  verified identity is involved. An unknown patient gets a temporary
 *  identity that never blocks care, and converting it later loses no
 *  transaction. Conflicting values from two sources stay VISIBLE with
 *  each value's source — never silently overwritten. A wrong merge is
 *  undone by an auditable unmerge that preserves history. */
public final class LongitudinalRules {
  private LongitudinalRules() {}
  public static final Set<String> DECIDER_ROLES = Set.of("identity_quality_officer","admin");
  public static final double CERTAIN_MATCH = 0.90;
  public static boolean mayDecideMerge(String role) { return role != null && DECIDER_ROLES.contains(role); }
  public static boolean highRisk(double similarityScore, String kindA, String kindB) {
    return similarityScore < CERTAIN_MATCH || "verified".equals(kindA) || "verified".equals(kindB);
  }
  public static boolean mergeApprovable(boolean deciderAllowed, boolean highRisk, String decidedBy, String secondApprover) {
    if (!deciderAllowed) return false;
    if (!highRisk) return decidedBy != null && !decidedBy.isBlank();
    return decidedBy != null && !decidedBy.isBlank()
        && secondApprover != null && !secondApprover.isBlank()
        && !secondApprover.equals(decidedBy);
  }
  public static boolean evidenceRequired(double similarityScore) { return similarityScore < 1.0; }
}
