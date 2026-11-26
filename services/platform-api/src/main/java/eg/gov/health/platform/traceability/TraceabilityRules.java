package eg.gov.health.platform.traceability;
/** Traceability rules (module 177). Every data entity has
 *  exactly ONE owner module — two owners means no owner, and
 *  the matrix refuses the second claim as a conflict to
 *  resolve, not a fact to store. Flows between producer and
 *  consumer are authorized explicitly; responsibility transfers
 *  between levels of care name the roles allowed to carry them. */
public final class TraceabilityRules {
  private TraceabilityRules() {}
  public static boolean ownershipConflict(String existingOwner, String claimedOwner) {
    return existingOwner != null && claimedOwner != null && !existingOwner.equals(claimedOwner);
  }
  public static boolean transferAuthorized(String authorizedRoles) {
    return authorizedRoles != null && !authorizedRoles.isBlank();
  }
}
