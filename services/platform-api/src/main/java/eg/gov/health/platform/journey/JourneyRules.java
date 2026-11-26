package eg.gov.health.platform.journey;
import java.util.*;
/** Cross-module journey rules (completion batch 1). A care
 *  journey moves through canonical stages — registration,
 *  encounter, triage, diagnostics, treatment, pharmacy,
 *  billing, discharge, followup — and never backwards: a
 *  discharge recorded before diagnostics exist is not a
 *  journey, it is disconnected paperwork. Completion requires
 *  the mandatory spine (encounter, a clinical stage, pharmacy,
 *  billing, discharge); a journey missing its spine cannot be
 *  declared complete, and the missing stages are named. */
public final class JourneyRules {
  private JourneyRules() {}
  public static final List<String> ORDER = List.of(
    "registration","encounter","triage","diagnostics","treatment","pharmacy","billing","discharge","followup");
  public static final List<String> MANDATORY = List.of("encounter","diagnostics","pharmacy","billing","discharge");
  public static int indexOf(String stepKey) { return ORDER.indexOf(stepKey); }
  public static boolean canRecord(Set<String> recorded, String stepKey) {
    int idx = indexOf(stepKey);
    if (idx < 0) return false;
    int max = recorded.stream().mapToInt(JourneyRules::indexOf).max().orElse(-1);
    return idx >= max;
  }
  public static List<String> missingMandatory(Set<String> recorded) {
    List<String> missing = new ArrayList<>();
    for (String m : MANDATORY) {
      if (m.equals("diagnostics")) { if (!recorded.contains("diagnostics") && !recorded.contains("treatment")) missing.add("diagnostics_or_treatment"); }
      else if (!recorded.contains(m)) missing.add(m);
    }
    return missing;
  }
}
