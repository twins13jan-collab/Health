package eg.gov.health.platform.ethicscommittees;
/** Ethics-committee rules (module 141). A committee decision is
 *  valid only with its quorum present, and only members whose
 *  conflict-of-interest declarations are on file may vote — a
 *  vote cast by a member with an undeclared interest is not a
 *  vote, it is a contamination of the record. Members with a
 *  conflict in a case recuse, and the recusal is recorded, not
 *  whispered. */
public final class EthicsRules {
  private EthicsRules() {}
  public static boolean quorumMet(Integer present, Integer quorumRequired) {
    return present != null && quorumRequired != null && present >= quorumRequired;
  }
  public static boolean votingPermitted(boolean coiDeclared) { return coiDeclared; }
}
