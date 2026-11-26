package eg.gov.health.platform.decisionregistry;
/** Decision-registry rules (module 163). A traceable decision
 *  carries its maker and their authority, the context, the
 *  alternatives that were considered and why this one was
 *  chosen — a decision without documented alternatives is an
 *  order, not a decision. History is never edited: review,
 *  amendment and revocation are new records with mandatory
 *  reasons, and the original stays readable forever. Committee
 *  decisions additionally require a clear conflicts position. */
public final class DecisionRegistryRules {
  private DecisionRegistryRules() {}
  public static boolean registerable(String maker, String authority, String context, String alternatives, String rationale,
                                     boolean committeeDecision, boolean coiClear) {
    if (maker == null || maker.isBlank() || authority == null || authority.isBlank()) return false;
    if (context == null || context.isBlank()) return false;
    if (alternatives == null || alternatives.isBlank()) return false;
    if (rationale == null || rationale.isBlank()) return false;
    return !committeeDecision || coiClear;
  }
  public static boolean amendmentValid(String reason) { return reason != null && !reason.isBlank(); }
}
