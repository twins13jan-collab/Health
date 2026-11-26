package eg.gov.health.platform.workforce;
import org.junit.jupiter.api.Test; import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
class ShiftRulesTest {
  ShiftRules.ShiftView shift(String s, String e) {
    return new ShiftRules.ShiftView(OffsetDateTime.parse(s), OffsetDateTime.parse(e), "scheduled");
  }
  @Test void overlapRejected() {
    var a = shift("2026-01-01T08:00:00+02:00","2026-01-01T20:00:00+02:00");
    var b = shift("2026-01-01T19:00:00+02:00","2026-01-02T07:00:00+02:00");
    assertFalse(ShiftRules.isAssignable(b, java.util.List.of(a)));
  }
  @Test void restUnder11HoursRejected() {
    var night = shift("2026-01-01T20:00:00+02:00","2026-01-02T08:00:00+02:00");
    var morning = shift("2026-01-02T16:00:00+02:00","2026-01-03T04:00:00+02:00");
    assertFalse(ShiftRules.isAssignable(morning, java.util.List.of(night)));
  }
  @Test void restOver11HoursAccepted() {
    var night = shift("2026-01-01T20:00:00+02:00","2026-01-02T08:00:00+02:00");
    var evening = shift("2026-01-02T20:00:00+02:00","2026-01-03T08:00:00+02:00");
    assertTrue(ShiftRules.isAssignable(evening, java.util.List.of(night)));
  }
  @Test void cancelledShiftsIgnored() {
    var cancelled = new ShiftRules.ShiftView(OffsetDateTime.parse("2026-01-01T08:00:00+02:00"), OffsetDateTime.parse("2026-01-01T20:00:00+02:00"), "cancelled");
    assertTrue(ShiftRules.isAssignable(shift("2026-01-01T09:00:00+02:00","2026-01-01T17:00:00+02:00"), java.util.List.of(cancelled)));
  }
}
