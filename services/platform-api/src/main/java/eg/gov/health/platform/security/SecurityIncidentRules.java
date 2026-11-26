package eg.gov.health.platform.security;
import java.util.Set;
/** Security & privacy incident rules (cycle 238). Evidence carries a
 *  hash and a custody chain and is never modified by investigators. A
 *  compromised account is contained only when its sessions and tokens
 *  are revoked. Ransomware recovery requires a VERIFIED clean backup —
 *  the existence of a backup proves nothing. A leak's scope stays
 *  "unconfirmed" until assessed; no unconfirmed number is ever
 *  published. Every notification decision — notify or not — carries a
 *  justification. Closure needs containment, verified recovery, and
 *  lessons learned with corrective actions closed. A monitoring failure
 *  is a visibility gap, never evidence that nothing happened. */
public final class SecurityIncidentRules {
  private SecurityIncidentRules() {}
  public static final Set<String> PRIVACY_ROLES = Set.of("privacy_officer","ciso","security_lead","admin");
  public static boolean mayAssessPrivacy(String role) { return role != null && PRIVACY_ROLES.contains(role); }
  public static boolean evidenceValid(String sha256Hash) {
    return sha256Hash != null && sha256Hash.matches("[0-9a-fA-F]{64}");
  }
  public static boolean containmentValid(String type, boolean sessionsRevoked) {
    return !"compromised_account".equals(type) || sessionsRevoked;
  }
  public static boolean recoveryValid(String type, boolean cleanBackupVerified) {
    return !"ransomware".equals(type) || cleanBackupVerified;
  }
  public static boolean notificationDecisionValid(String justification) {
    return justification != null && !justification.isBlank();
  }
  public static boolean closable(boolean hasContainment, boolean recovered, boolean lessonsRecorded, boolean correctiveClosed) {
    return hasContainment && recovered && lessonsRecorded && correctiveClosed;
  }
}
