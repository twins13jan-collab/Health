package eg.gov.health.platform.eligibility;
import java.util.*;
/** Eligibility-before-booking rules (module 183). A booking is
 *  eligible only when the service REALLY exists at that slot:
 *  offered, licensed, with capacity, staff, equipment and
 *  consumables — and the patient is clinically eligible. Unmet
 *  preparation is a visible condition, not a silent block. Coverage
 *  and pre-authorization are verified and reported, but they NEVER
 *  enter the eligibility decision: money does not overrule the
 *  clinician. Every failed check is named in the restrictions so
 *  the requester sees exactly why a service is unavailable, and the
 *  check records its time and source. */
public final class EligibilityRules {
  private EligibilityRules() {}
  public static List<String> restrictions(boolean offered, boolean license, boolean capacity, boolean staff,
                                          boolean equipment, boolean consumables, boolean clinical) {
    var out = new ArrayList<String>();
    if (!offered) out.add("service_not_offered");
    if (!license) out.add("license_invalid");
    if (!capacity) out.add("no_capacity");
    if (!staff) out.add("staff_unavailable");
    if (!equipment) out.add("equipment_unavailable");
    if (!consumables) out.add("consumables_unavailable");
    if (!clinical) out.add("clinically_ineligible");
    return out;
  }
  public static String decide(List<String> restrictions, boolean preparationMet) {
    if (!restrictions.isEmpty()) return "ineligible";
    return preparationMet ? "eligible" : "eligible_with_conditions";
  }
  /** Coverage is deliberately NOT an input to decide(): it is reported alongside, never inside, the decision. */
  public static String financialNote(String coverageStatus) {
    return switch (coverageStatus == null ? "unknown" : coverageStatus) {
      case "covered" -> "coverage confirmed";
      case "preauth_required" -> "financial pre-authorization required — a financial step, not a clinical barrier";
      case "not_covered" -> "not covered — reported for the financial pathway; the clinical decision stands";
      default -> "coverage unknown — verify before the appointment";
    };
  }
}
