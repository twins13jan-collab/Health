package eg.gov.health.platform.anomalyregistry;
/** Congenital-anomalies registry rules (module 169). The registry
 *  distinguishes confirmed from suspected cases everywhere it
 *  counts: published trend figures are confirmed cases, with
 *  suspected shown separately — never merged into a scarier
 *  headline. Trend analysis describes patterns; a CAUSAL claim
 *  enters the record only with supporting evidence documented,
 *  because an unsupported causal statement about a birth defect
 *  harms families and misdirects prevention. Linking a case to
 *  pregnancy, birth and newborn records is permission-bound. */
public final class RegistryRules {
  private RegistryRules() {}
  public static boolean causalClaimPermitted(String causalClaim, String supportingEvidence) {
    if (causalClaim == null || causalClaim.isBlank()) return true;
    return supportingEvidence != null && !supportingEvidence.isBlank();
  }
  public static boolean linkagePermitted(String role) {
    return "admin".equals(role) || "registry_officer".equals(role) || "physician".equals(role);
  }
}
