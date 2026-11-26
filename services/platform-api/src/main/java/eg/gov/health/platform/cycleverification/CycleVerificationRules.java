package eg.gov.health.platform.cycleverification;
import java.util.*;
/** Cycle-completeness rules (module 180). A service cycle is
 *  complete only when every stage is evidenced — request,
 *  triage, approval, execution, result and follow-up — and the
 *  evidence references exist. A screen that rendered or an API
 *  that returned 200 is not evidence the process completed: the
 *  catalogue says so outright, and every stuck instance this
 *  module has ever caught was "visible" in some UI the whole
 *  time. Stuck and cancelled instances are managed in the open,
 *  never left to age silently. */
public final class CycleVerificationRules {
  private CycleVerificationRules() {}
  public static List<String> missingStages(boolean request, boolean triage, boolean approval,
                                           boolean execution, boolean result, boolean followup) {
    List<String> m = new ArrayList<>();
    if (!request) m.add("request");
    if (!triage) m.add("triage");
    if (!approval) m.add("approval");
    if (!execution) m.add("execution");
    if (!result) m.add("result");
    if (!followup) m.add("followup");
    return m;
  }
  public static boolean complete(boolean request, boolean triage, boolean approval, boolean execution,
                                 boolean result, boolean followup, String evidenceRefs) {
    return missingStages(request, triage, approval, execution, result, followup).isEmpty()
      && evidenceRefs != null && !evidenceRefs.isBlank();
  }
}
