package eg.gov.health.platform.clinical;
import org.junit.jupiter.api.Test;
import java.time.OffsetDateTime; import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class ErRulesTest {
  @Test void levelsAndTargets() {
    assertTrue(ErRules.validLevel(1)); assertTrue(ErRules.validLevel(5));
    assertFalse(ErRules.validLevel(0)); assertFalse(ErRules.validLevel(6)); assertFalse(ErRules.validLevel(null));
    assertEquals(0, ErRules.targetMinutes(1));
    assertEquals(10, ErRules.targetMinutes(2));
    assertEquals(60, ErRules.targetMinutes(3));
    assertEquals(240, ErRules.targetMinutes(5));
  }
  @Test void breachOnlyWhileWaitingPastTarget() {
    assertTrue(ErRules.breached(2, 11, "open"));
    assertFalse(ErRules.breached(2, 10, "open"));
    assertFalse(ErRules.breached(2, 90, "in_progress")); // already with a clinician
    assertFalse(ErRules.breached(null, 90, "open"));
    assertTrue(ErRules.breached(1, 1, "open")); // resuscitation never waits
  }
  @Test void queueOrdersByPriorityThenLongestWait() {
    var now = OffsetDateTime.now();
    assertEquals(45, ErRules.waitMinutes(now.minusMinutes(45), now));
    assertEquals(0, ErRules.waitMinutes(now.plusMinutes(3), now));
    Map<String, Object> l3 = new HashMap<>(Map.of("status", "open", "triageLevel", 3, "waitMinutes", 30L));
    Map<String, Object> l1 = new HashMap<>(Map.of("status", "open", "triageLevel", 1, "waitMinutes", 2L));
    Map<String, Object> l3long = new HashMap<>(Map.of("status", "open", "triageLevel", 3, "waitMinutes", 90L));
    Map<String, Object> treating = new HashMap<>(Map.of("status", "in_progress", "triageLevel", 1, "waitMinutes", 5L));
    var rows = new ArrayList<>(List.of(l3, treating, l3long, l1));
    rows.sort(ErRules.queueOrder());
    assertSame(l1, rows.get(0));
    assertSame(l3long, rows.get(1));
    assertSame(l3, rows.get(2));
    assertSame(treating, rows.get(3));
  }
}
