package eg.gov.health.platform.uxgov;
/** UX governance rules (module 136). Roles and their work scenarios
 *  are managed explicitly; critical tasks carry a time target and a
 *  measured time, and a critical task slower than its target is
 *  flagged for optimization — speed of critical work is a safety
 *  property. A scenario is validated only by a completed usability
 *  session with no open blocker findings: an open blocker blocks
 *  sign-off, however polished everything else looks. Errors are
 *  prevented where possible and correctable where not — findings
 *  track that work to closure. */
public final class UxRules {
  private UxRules() {}
  public static boolean criticalTaskFastEnough(boolean critical, Integer targetSeconds, Integer measuredSeconds) {
    if (!critical) return true;
    return targetSeconds != null && measuredSeconds != null && measuredSeconds <= targetSeconds;
  }
  public static boolean scenarioValidatable(boolean anySessionCompleted, long openBlockers) {
    return anySessionCompleted && openBlockers == 0;
  }
}
