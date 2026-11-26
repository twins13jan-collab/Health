package eg.gov.health.platform.porterage;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
class SupportBoardRulesTest {
  private static final OffsetDateTime NOW = OffsetDateTime.parse("2026-11-23T12:00:00+02:00");
  @Test void priorityOrder() {
    assertEquals(0, SupportBoardRules.priorityRank("critical"));
    assertEquals(1, SupportBoardRules.priorityRank("urgent"));
    assertEquals(2, SupportBoardRules.priorityRank("routine"));
    assertEquals(3, SupportBoardRules.priorityRank(null));
  }
  @Test void breachUsesPerPriorityTargets() {
    // routine target 45: 60 minutes waiting breaches; critical target 10: 11 breaches, 9 does not
    assertTrue(SupportBoardRules.waitBreached("routine", NOW.minusMinutes(60), NOW));
    assertFalse(SupportBoardRules.waitBreached("routine", NOW.minusMinutes(44), NOW));
    assertTrue(SupportBoardRules.waitBreached("critical", NOW.minusMinutes(11), NOW));
    assertFalse(SupportBoardRules.waitBreached("critical", NOW.minusMinutes(9), NOW));
  }
  @Test void breachedOutranksCriticalOutranksPickedUp() {
    assertEquals(0, SupportBoardRules.transportRank("requested", "routine", true));
    assertEquals(1, SupportBoardRules.transportRank("assigned", "critical", false));
    assertEquals(2, SupportBoardRules.transportRank("picked_up", "routine", false));
    assertTrue(SupportBoardRules.transportRank("requested", "urgent", false) > SupportBoardRules.transportRank("picked_up", "routine", false));
    assertEquals(9, SupportBoardRules.transportRank("delivered", "critical", true));
  }
  @Test void roomOverdueAfterTwoHoursOnlyWhileUnready() {
    assertTrue(SupportBoardRules.roomOverdue("pending", NOW.minusMinutes(121), NOW));
    assertFalse(SupportBoardRules.roomOverdue("pending", NOW.minusMinutes(119), NOW));
    assertFalse(SupportBoardRules.roomOverdue("ready", NOW.minusMinutes(300), NOW));
  }
  @Test void laundryStatesNameLoss() {
    assertEquals("in_process", SupportBoardRules.laundryState("sent", 10, null));
    assertEquals("received_with_loss", SupportBoardRules.laundryState("returned", 10, 8));
    assertEquals("received_complete", SupportBoardRules.laundryState("returned", 10, 10));
  }
}
