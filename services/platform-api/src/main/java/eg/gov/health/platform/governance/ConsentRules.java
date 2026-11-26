package eg.gov.health.platform.governance;
import java.time.OffsetDateTime;
/** Consent validity (cycle 218): a share is allowed ONLY when an active
 *  consent exists for the same purpose + recipient + data scope, inside its
 *  validity window, and not revoked. Revocation stops FUTURE use; it never
 *  erases processing that had a lawful basis when it happened. */
public final class ConsentRules {
  private ConsentRules() {}
  public record ConsentView(String purpose, String recipient, String dataScope, String status,
                            OffsetDateTime effectiveFrom, OffsetDateTime expiresAt) {}
  public static boolean isShareAllowed(ConsentView c, String purpose, String recipient, String dataScope, OffsetDateTime now) {
    if (c == null || !"active".equals(c.status())) return false;
    if (!purpose.equals(c.purpose()) || !recipient.equals(c.recipient()) || !dataScope.equals(c.dataScope())) return false;
    if (c.effectiveFrom() != null && now.isBefore(c.effectiveFrom())) return false;
    return c.expiresAt() == null || now.isBefore(c.expiresAt());
  }
  public static boolean isBreakGlassJustifiable(String reason, int requestedMinutes) {
    return reason != null && reason.trim().length() >= 10 && requestedMinutes > 0 && requestedMinutes <= 240;
  }
}
