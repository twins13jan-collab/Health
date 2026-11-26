package eg.gov.health.platform.softwarelifecycle;
/** Software-lifecycle rules (module 135). Production is a
 *  promise: nothing reaches it unreviewed, unsigned, or with
 *  configuration drift between environments — and a
 *  third-party component ships only with its licence on file.
 *  Rollback is a first-class, reasoned act, not an admission
 *  whispered in a chat log. */
public final class LifecycleRules {
  private LifecycleRules() {}
  public static boolean productionDeployable(boolean codeReviewed, boolean buildSigned, boolean configDrift,
                                             boolean thirdParty, String licenceRef) {
    if (!codeReviewed || !buildSigned || configDrift) return false;
    return !thirdParty || (licenceRef != null && !licenceRef.isBlank());
  }
  public static boolean rollbackValid(String reason) { return reason != null && !reason.isBlank(); }
}
