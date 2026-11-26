package eg.gov.health.platform.pharmacy;
import java.util.*;
/** Drug-interaction + duplication guard (cycle 222, medication reconciliation).
 *  Advisory to the verifying pharmacist: a flagged pair never auto-blocks a
 *  prescription silently — it requires pharmacist verification with reason. */
public final class InteractionRules {
  private InteractionRules() {}
  static final Set<Set<String>> SEVERE = Set.of(
    Set.of("WARFARIN","ASPIRIN"), Set.of("WARFARIN","IBUPROFEN"),
    Set.of("METHOTREXATE","TRIMETHOPRIM"), Set.of("SILDENAFIL","NITROGLYCERIN"),
    Set.of("CLARITHROMYCIN","SIMVASTATIN"));
  public static Optional<String> severeInteraction(String drugCode, Collection<String> activeDrugCodes) {
    if (drugCode == null) return Optional.empty();
    String d = drugCode.toUpperCase();
    for (String other : activeDrugCodes) {
      if (other == null) continue;
      if (SEVERE.contains(Set.of(d, other.toUpperCase()))) return Optional.of(d + " + " + other.toUpperCase());
    }
    return Optional.empty();
  }
  public static boolean isDuplicate(String drugCode, Collection<String> activeDrugCodes) {
    if (drugCode == null) return false;
    return activeDrugCodes.stream().anyMatch(x -> x != null && x.equalsIgnoreCase(drugCode));
  }
}
