package eg.gov.health.platform.unknownpatient;
import java.util.Set;
/** Unknown-identity & humanitarian-case rules (module 185). Care
 *  starts under a safe temporary identity — treatment never waits
 *  for paperwork. The temporary identity merges into the verified
 *  record only on documented verification evidence, and the
 *  verifier is never the person who created the temporary identity.
 *  A merged identity is closed to further merges. Social-protection
 *  data (separated children, vulnerable cases) is restricted to
 *  social-work and administration roles. Every verification and
 *  modification step is chained. Deaths without documents follow
 *  the legal procedure with the authority notified. */
public final class UnknownPatientRules {
  private UnknownPatientRules() {}
  public static final Set<String> PROTECTION_ROLES = Set.of("social_worker","admin");
  public static boolean mergeAllowed(String status, String evidence, String verifier, String creator) {
    return "active".equals(status) && evidence != null && !evidence.isBlank()
      && verifier != null && creator != null && !verifier.equals(creator);
  }
  public static boolean protectionVisible(boolean protectionFlag, String role) {
    return !protectionFlag || (role != null && PROTECTION_ROLES.contains(role));
  }
}
