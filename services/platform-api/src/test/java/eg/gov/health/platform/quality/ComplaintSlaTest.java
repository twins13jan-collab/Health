package eg.gov.health.platform.quality;
import org.junit.jupiter.api.Test; import java.time.OffsetDateTime;
import static org.junit.jupiter.api.Assertions.*;
class ComplaintSlaTest {
  @Test void dueDatesBySeverity() {
    var t = OffsetDateTime.parse("2026-01-01T10:00:00+02:00");
    assertEquals(t.plusDays(3), ComplaintSla.dueAt(t, "critical"));
    assertEquals(t.plusDays(30), ComplaintSla.dueAt(t, "low"));
  }
  @Test void breachOnlyWhenOpenAndOverdue() {
    var t = OffsetDateTime.parse("2026-01-01T10:00:00+02:00");
    assertTrue(ComplaintSla.isBreached(t, "critical", "investigating", t.plusDays(4)));
    assertFalse(ComplaintSla.isBreached(t, "critical", "resolved", t.plusDays(4)));
    assertFalse(ComplaintSla.isBreached(t, "critical", "investigating", t.plusDays(2)));
  }
}
