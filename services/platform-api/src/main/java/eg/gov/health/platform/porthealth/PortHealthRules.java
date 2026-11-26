package eg.gov.health.platform.porthealth;
import java.time.*;
/** Port-health & quarantine rules (module 145). A high-risk
 *  symptomatic traveler is isolated or referred — never cleared at
 *  the desk. Quarantine runs its legal course: early release needs
 *  an authorized releaser and a documented reason, on the record.
 *  Travel data is held at the minimum — a traveler code and a
 *  travel reference, no identity documents in this module — and
 *  referred cases carry a coordination note with the port
 *  authorities. */
public final class PortHealthRules {
  private PortHealthRules() {}
  public static boolean screeningPermitted(String riskLevel, String screeningResult) {
    return !("high".equals(riskLevel) && "cleared".equals(screeningResult));
  }
  public static boolean earlyReleasePermitted(OffsetDateTime now, OffsetDateTime endsAt, String releasedBy, String reason) {
    if (now == null || endsAt == null || !now.isBefore(endsAt)) return true; // not early
    return releasedBy != null && !releasedBy.isBlank() && reason != null && !reason.isBlank();
  }
  public static boolean quarantineComplete(OffsetDateTime now, OffsetDateTime endsAt) {
    return now != null && endsAt != null && !now.isBefore(endsAt);
  }
}
