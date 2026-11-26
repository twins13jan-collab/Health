package eg.gov.health.platform.citizenportal;
import java.time.*;
/** Citizen-portal rules (module 111). Nothing is disclosed to an
 *  unverified identity. A representative sees only what an approved,
 *  unexpired representation covers — scope results does not open
 *  prescriptions. Messages travel only on channels the citizen
 *  enabled, and clinical alerts need the clinical-alerts consent on
 *  that channel. Accessible formats are part of the account, not an
 *  afterthought. */
public final class PortalRules {
  private PortalRules() {}
  public static boolean selfAccess(boolean identityVerified, String accountStatus) {
    return identityVerified && "active".equals(accountStatus);
  }
  public static boolean representativeAccess(boolean approved, LocalDate validUntil, String scope, String resourceType, LocalDate today) {
    if (!approved || validUntil == null || today == null || validUntil.isBefore(today)) return false;
    if ("all".equals(scope)) return true;
    return switch (resourceType) {
      case "lab_result", "vaccination" -> "results".equals(scope);
      case "prescription" -> "prescriptions".equals(scope);
      case "summary" -> "results".equals(scope) || "appointments".equals(scope);
      default -> false;
    };
  }
  public static boolean channelConsented(boolean preferenceExists, boolean enabled, boolean clinical, boolean clinicalAlerts) {
    if (!preferenceExists || !enabled) return false;
    return !clinical || clinicalAlerts;
  }
}
