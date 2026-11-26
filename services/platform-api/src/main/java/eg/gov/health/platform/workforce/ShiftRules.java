package eg.gov.health.platform.workforce;
import java.time.*;
/** Safe-rostering rules (cycle 231): no overlapping shifts for the same
 *  staff member, and a minimum 11-hour rest between the end of one shift
 *  and the start of the next. Violations are rejected, never warned-only. */
public final class ShiftRules {
  private ShiftRules() {}
  public static final long MIN_REST_HOURS = 11;
  public record ShiftView(OffsetDateTime startsAt, OffsetDateTime endsAt, String status) {}
  public static boolean overlaps(ShiftView a, ShiftView b) {
    return !"cancelled".equals(b.status()) && a.startsAt().isBefore(b.endsAt()) && b.startsAt().isBefore(a.endsAt());
  }
  public static boolean hasEnoughRest(ShiftView candidate, ShiftView other) {
    if ("cancelled".equals(other.status())) return true;
    if (overlaps(candidate, other)) return false;
    long gapAfter = Math.abs(Duration.between(candidate.endsAt(), other.startsAt()).toHours());
    long gapBefore = Math.abs(Duration.between(other.endsAt(), candidate.startsAt()).toHours());
    long gap = candidate.startsAt().isAfter(other.endsAt()) ? gapBefore : gapAfter;
    return gap >= MIN_REST_HOURS;
  }
  public static boolean isAssignable(ShiftView candidate, java.util.List<ShiftView> existing) {
    return existing.stream().allMatch(o -> hasEnoughRest(candidate, o));
  }
}
