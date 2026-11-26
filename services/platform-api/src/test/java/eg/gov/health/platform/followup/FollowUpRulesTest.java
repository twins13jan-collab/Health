package eg.gov.health.platform.followup;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
class FollowUpRulesTest {
  private final OffsetDateTime now = OffsetDateTime.parse("2026-11-22T12:00+02:00");
  @Test void overdueTasksEscalateAndUnreachableEscalates() {
    assertEquals("escalated", FollowUpRules.taskState("pending", now.minusDays(1), now, 0));
    assertEquals("pending", FollowUpRules.taskState("pending", now.plusDays(1), now, 0));
    assertEquals("unreachable", FollowUpRules.taskState("pending", now.plusDays(1), now, 3));
    assertEquals("done", FollowUpRules.taskState("done", now.minusDays(5), now, 3));
  }
  @Test void abnormalUnnotifiedIsAnOpenBreach() {
    assertTrue(FollowUpRules.abnormalNotificationBreach(true, true, false));
    assertFalse(FollowUpRules.abnormalNotificationBreach(true, true, true));
    assertFalse(FollowUpRules.abnormalNotificationBreach(false, true, false));
  }
  @Test void plansCloseOnlyWithNothingOpen() {
    assertTrue(FollowUpRules.closable(0, 0, 0));
    assertFalse(FollowUpRules.closable(1, 0, 0));
    assertFalse(FollowUpRules.closable(0, 1, 0));
    assertFalse(FollowUpRules.closable(0, 0, 1));
  }
}
