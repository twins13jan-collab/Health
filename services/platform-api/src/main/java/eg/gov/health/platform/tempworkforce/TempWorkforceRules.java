package eg.gov.health.platform.tempworkforce;
import java.time.*;
/** Temporary-workforce marketplace rules (module 171). Nobody
 *  works a shift on a marketplace promise: identity and
 *  qualifications are verified, and the licence must be valid ON
 *  the shift date. The assignment stays inside the provider's
 *  scope of practice. Double-assignment is prevented — overlapping
 *  shifts for the same provider are refused — and working-time
 *  limits are enforced across the rolling week: an assignment that
 *  pushes the provider past 60 hours in 7 days is refused, however
 *  urgent the gap. */
public final class TempWorkforceRules {
  private TempWorkforceRules() {}
  public static final double MAX_WEEKLY_HOURS = 60.0;
  public static boolean providerAssignable(boolean identityVerified, boolean qualificationsVerified, LocalDate licenseValidUntil, LocalDate shiftDate, String status) {
    return identityVerified && qualificationsVerified && "verified".equals(status)
      && licenseValidUntil != null && shiftDate != null && !licenseValidUntil.isBefore(shiftDate);
  }
  public static boolean overlaps(OffsetDateTime aStart, OffsetDateTime aEnd, OffsetDateTime bStart, OffsetDateTime bEnd) {
    return aStart.isBefore(bEnd) && bStart.isBefore(aEnd);
  }
  public static double hours(OffsetDateTime start, OffsetDateTime end) {
    return Duration.between(start, end).toMinutes() / 60.0;
  }
  public static boolean withinWeeklyLimit(double existingHoursInWindow, OffsetDateTime start, OffsetDateTime end) {
    return existingHoursInWindow + hours(start, end) <= MAX_WEEKLY_HOURS;
  }
  public static boolean scopeMatches(String providerScope, String assignmentScope) {
    return providerScope != null && assignmentScope != null && providerScope.contains(assignmentScope);
  }
}
