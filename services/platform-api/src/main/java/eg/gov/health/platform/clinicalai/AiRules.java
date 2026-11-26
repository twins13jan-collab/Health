package eg.gov.health.platform.clinicalai;
import java.math.BigDecimal; import java.util.Set;
/** Clinical AI rules (cycle 233). The model owner manages versions
 *  but never approves clinical use alone — a competent committee
 *  approves a validated model for a defined scope. The practitioner
 *  reviews every recommendation and holds the final clinical
 *  decision; NO high-risk automated decision takes effect without the
 *  required human supervision and approval. Incomplete inputs or
 *  inputs outside the validated scope make the model ABSTAIN rather
 *  than guess. Performance drift triggers restriction or suspension.
 *  A model outage never blocks the ordinary clinical path. Every
 *  inference records the model version, the policy and the actor. */
public final class AiRules {
  private AiRules() {}
  public static final BigDecimal DRIFT_SUSPEND_THRESHOLD = new BigDecimal("0.10");
  public static final Set<String> COMMITTEE_ROLES = Set.of("clinical_ai_committee","chief_medical_officer","admin");
  public static boolean mayApproveClinicalUse(String role) { return role != null && COMMITTEE_ROLES.contains(role); }
  public static boolean inferable(String modelStatus, boolean committeeApproved) {
    return "approved".equals(modelStatus) && committeeApproved;
  }
  public static boolean mustAbstain(boolean inputsComplete, boolean inScope) {
    return !inputsComplete || !inScope;
  }
  public static boolean driftSuspends(BigDecimal driftMetric) {
    return driftMetric != null && driftMetric.compareTo(DRIFT_SUSPEND_THRESHOLD) >= 0;
  }
  public static boolean applicable(String reviewDecision) {
    // Only a human accept/modify decision may carry an inference into care.
    return "accepted".equals(reviewDecision) || "modified".equals(reviewDecision);
  }
}
