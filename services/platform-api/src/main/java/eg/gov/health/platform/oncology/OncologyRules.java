package eg.gov.health.platform.oncology;
import java.math.BigDecimal; import java.time.*; import java.util.Set;
/** Oncology rules (cycle 243). Every treatment cycle is bound to a
 *  protocol VERSION that is medically approved, in date, and sourced
 *  — a protocol merely existing in the system is NOT a medical
 *  approval, and the system never generates a therapeutic dose: the
 *  dose is what the consultant approved under that protocol version.
 *  Pre-treatment verification checks weight/BSA, functions, labs,
 *  allergy and consent; weight change or labs outside the protocol
 *  conditions SUSPEND verification for physician review. Preparation
 *  and administration are independently verified per risk level, and
 *  the oncology pharmacist reviews preparation and compatibility.
 *  Prescribed vs administered dose is recorded with any variance,
 *  its reason and its approver. An unavailable or recalled drug
 *  blocks dispensing; a substitute needs a medical decision. A
 *  postponed session records its reason and impact — never an
 *  unapproved automatic reschedule. Toxicity is tied to the
 *  continue/modify/stop decision. A technical user can never modify
 *  a protocol or a dose. */
public final class OncologyRules {
  private OncologyRules() {}
  public static final Set<String> DOSE_ROLES = Set.of("oncologist","oncology_pharmacist","admin");
  public static final Set<String> PROTOCOL_ROLES = Set.of("oncologist","admin");
  public static boolean mayHandleDose(String role) { return role != null && DOSE_ROLES.contains(role); }
  public static boolean mayModifyProtocol(String role) { return role != null && PROTOCOL_ROLES.contains(role); }
  public static boolean protocolUsable(boolean medicallyApproved, LocalDate validUntil, String status, LocalDate today) {
    return medicallyApproved && "active".equals(status) && validUntil != null && today != null && !today.isAfter(validUntil);
  }
  public static boolean verificationSuspended(BigDecimal weightChangePct, BigDecimal maxAllowedPct, boolean labsWithinConditions) {
    if (!labsWithinConditions) return true;
    return weightChangePct != null && maxAllowedPct != null
        && weightChangePct.abs().compareTo(maxAllowedPct) > 0;
  }
  public static boolean independentVerifier(String prescriber, String verifier) {
    return verifier != null && !verifier.isBlank() && !verifier.equals(prescriber);
  }
  public static boolean varianceDocumented(BigDecimal variance, String reason, String approvedBy) {
    if (variance == null || variance.compareTo(BigDecimal.ZERO) == 0) return true;
    return reason != null && !reason.isBlank() && approvedBy != null && !approvedBy.isBlank();
  }
  public static boolean dispensable(boolean drugAvailable, boolean recalled, String substitute, String substituteDecisionBy) {
    if (recalled) return substitute != null && substituteDecisionBy != null; // recalled: only via a decided substitute
    if (!drugAvailable) return substitute != null && substituteDecisionBy != null;
    return true;
  }
}
