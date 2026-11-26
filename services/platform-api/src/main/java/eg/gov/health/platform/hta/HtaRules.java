package eg.gov.health.platform.hta;
/** HTA rules (module 166). A recommendation is the end of an
 *  evidence process, not an opinion: the panel cannot appraise
 *  while any member's conflict-of-interest declaration is missing,
 *  stakeholders must have been consulted, and the strength of the
 *  recommendation may not exceed the quality of the evidence —
 *  adoption demands at least moderate-quality evidence of
 *  effectiveness with acceptable safety; weak evidence yields
 *  research-only or rejection, however enthusiastic the requester. */
public final class HtaRules {
  private HtaRules() {}
  public static boolean appraisalPermitted(long panelMembers, long undeclaredCoi, boolean stakeholdersConsulted) {
    return panelMembers > 0 && undeclaredCoi == 0 && stakeholdersConsulted;
  }
  public static boolean recommendationPermitted(String recommendation, String evidenceQuality, String effectiveness, String safety) {
    if ("adopt".equals(recommendation) || "adopt_with_restrictions".equals(recommendation)) {
      boolean qualityOk = "high".equals(evidenceQuality) || "moderate".equals(evidenceQuality);
      return qualityOk && "effective".equals(effectiveness) && "acceptable".equals(safety);
    }
    return true; // reject / research_only are always available conclusions
  }
}
