package eg.gov.health.platform.iamdeep;
import java.time.*;
/** IAM rules (module 63). Access is granted for a named
 *  purpose, within a scope, by someone other than its holder —
 *  self-granted access is not access control, it is its
 *  absence. A temporary grant dies at its expiry whether or
 *  not anyone remembers to revoke it: expiry is computed, not
 *  remembered. Joiner, mover and leaver events drive the
 *  lifecycle; a leaver's sessions are revoked, not left to age
 *  out. */
public final class IamRules {
  private IamRules() {}
  public static boolean grantable(String purpose, String scope, String subject, String approver,
                                  boolean temporary, OffsetDateTime expiresAt) {
    if (purpose == null || purpose.isBlank() || scope == null || scope.isBlank()) return false;
    if (subject != null && subject.equals(approver)) return false; // no self-grant
    return !temporary || expiresAt != null;
  }
  public static boolean active(boolean temporary, OffsetDateTime expiresAt, OffsetDateTime now) {
    return !temporary || (expiresAt != null && now != null && now.isBefore(expiresAt));
  }
}
