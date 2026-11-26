package eg.gov.health.platform.cycles;
import java.util.*;
/** The unified operating-cycle state machine of Catalog v6.
 *  Rules: only listed transitions are legal; terminal states never leave;
 *  cancellation / failure / correction / reconciliation / hold REQUIRE a
 *  documented reason; pending_review -> completed is the review gate
 *  (a mandatory step that cannot be skipped from in_progress). */
public final class CycleStateMachine {
  private CycleStateMachine() {}
  public static final List<String> STATUSES = List.of("draft","open","in_progress","on_hold","pending_review",
    "completed","closed","cancelled","failed","corrected","reconciled");
  public static final Set<String> TERMINAL = Set.of("closed","cancelled");
  public static final Map<String, List<String>> TRANSITIONS = Map.ofEntries(
    Map.entry("draft", List.of("open")),
    Map.entry("open", List.of("in_progress","cancelled")),
    Map.entry("in_progress", List.of("on_hold","pending_review","failed")),
    Map.entry("on_hold", List.of("in_progress")),
    Map.entry("pending_review", List.of("in_progress","completed")),
    Map.entry("completed", List.of("closed","corrected")),
    Map.entry("corrected", List.of("closed")),
    Map.entry("failed", List.of("reconciled")),
    Map.entry("reconciled", List.of("closed")),
    Map.entry("cancelled", List.of("reconciled")),
    Map.entry("closed", List.of()));
  public static final Set<String> REASON_REQUIRED = Set.of("cancelled","failed","corrected","reconciled","on_hold");
  public static boolean isLegal(String from, String to) {
    return TRANSITIONS.getOrDefault(from, List.of()).contains(to);
  }
}
