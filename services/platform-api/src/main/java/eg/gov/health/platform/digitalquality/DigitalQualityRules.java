package eg.gov.health.platform.digitalquality;
/** Digital-experience quality rules (module 175). A surface
 *  passes its quality gate on measured evidence: task completion
 *  of at least 90%, p95 load within 3 seconds, and API
 *  connectivity verified against REAL data. The cardinal rule of
 *  this module: demonstration data must never appear as
 *  operational data — a production surface with an unresolved
 *  demo-data incident is non-compliant until the incident is
 *  resolved, whatever its other metrics say. Interfaces are also
 *  tested with actual users before they are declared ready. */
public final class DigitalQualityRules {
  private DigitalQualityRules() {}
  public static final double MIN_TASK_COMPLETION = 90.0;
  public static final int MAX_P95_LOAD_MS = 3000;
  public static boolean checkPasses(Double taskCompletionPct, Integer p95LoadMs, boolean apiConnectivityVerified) {
    return taskCompletionPct != null && taskCompletionPct >= MIN_TASK_COMPLETION
      && p95LoadMs != null && p95LoadMs <= MAX_P95_LOAD_MS && apiConnectivityVerified;
  }
  public static boolean surfaceCompliant(String environment, long unresolvedDemoIncidents, boolean latestCheckPassed) {
    if ("production".equals(environment) && unresolvedDemoIncidents > 0) return false;
    return latestCheckPassed;
  }
}
