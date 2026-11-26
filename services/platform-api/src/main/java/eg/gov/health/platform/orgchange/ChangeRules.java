package eg.gov.health.platform.orgchange;
/** Organizational-change rules (module 162). A change goes live
 *  only when its impact on roles, policies and data is analyzed,
 *  the affected people have been communicated with, at least 80%
 *  of them are trained, and the working procedures and delegations
 *  are updated — technology first, people later is how rollouts
 *  die. Adoption is declared from MEASURED use, not from the
 *  launch date: real usage of 70% or more and zero open workaround
 *  reports, because a workaround is the organization voting with
 *  its feet. */
public final class ChangeRules {
  private ChangeRules() {}
  public static final double TRAINING_THRESHOLD = 0.80;
  public static final double ADOPTION_THRESHOLD = 70.0;
  public static boolean goLivePermitted(String impactAnalysis, boolean communicated, long trained, long affected, boolean sopUpdated) {
    return impactAnalysis != null && !impactAnalysis.isBlank() && communicated && sopUpdated
      && affected > 0 && (double) trained / affected >= TRAINING_THRESHOLD;
  }
  public static boolean adoptionDeclared(Double activeUsersPct, long workaroundReports) {
    return activeUsersPct != null && activeUsersPct >= ADOPTION_THRESHOLD && workaroundReports == 0;
  }
}
