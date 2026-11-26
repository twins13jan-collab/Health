package eg.gov.health.platform.homecare;
import eg.gov.health.platform.criticalcare.IcuRules;
import java.time.*;
/** Home-care rules (cycle 253). Every visit is bound to an approved plan,
 *  a team and recorded times; a missed-access visit always creates a
 *  follow-up duty; deterioration at home triggers escalation to the
 *  ambulance/facility; remote readings obey the same freshness law as the
 *  ICU — a disconnected device never shows its last reading as current;
 *  the exact home address is never stored (coarse zone only). */
public final class HomeCareRules {
  private HomeCareRules() {}
  /** Visits run only under an active, physician-approved plan. */
  public static boolean visitsAllowed(String planStatus) { return "active".equals(planStatus); }
  /** A visit completes only after arrival, and departure follows arrival. */
  public static boolean visitTimesValid(OffsetDateTime arrivedAt, OffsetDateTime leftAt) {
    return arrivedAt != null && leftAt != null && !leftAt.isBefore(arrivedAt);
  }
  /** Deterioration observed at home escalates immediately. */
  public static boolean escalationRequired(String outcome) {
    return "deteriorated".equals(outcome) || "emergency".equals(outcome);
  }
  /** Device maintenance is overdue strictly after its due date. */
  public static boolean maintenanceOverdue(LocalDate due, LocalDate today) {
    return due != null && today != null && today.isAfter(due);
  }
  /** Freshness of a remote reading — same law as cycle 221. */
  public static IcuRules.Freshness freshness(OffsetDateTime sourceTime, OffsetDateTime now) {
    return IcuRules.freshness(sourceTime, now);
  }
  public static boolean mayPresentAsLive(IcuRules.Freshness f) { return IcuRules.mayPresentAsLive(f); }
}
