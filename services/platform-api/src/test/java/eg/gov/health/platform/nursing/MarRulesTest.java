package eg.gov.health.platform.nursing;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class MarRulesTest {
  @Test void frequencyMappingIsTotal() {
    assertEquals(2, MarRules.timesFor("BID").size());
    assertEquals(3, MarRules.timesFor("TID").size());
    assertEquals(4, MarRules.timesFor("q6h").size());
    assertEquals(3, MarRules.timesFor("q8h").size());
    assertEquals(2, MarRules.timesFor("مرتين يوميًا").size());
    assertEquals(List.of(LocalTime.of(9, 0)), MarRules.timesFor("once daily"));
    assertEquals(List.of(LocalTime.of(9, 0)), MarRules.timesFor(null));
    assertEquals(List.of(LocalTime.of(9, 0)), MarRules.timesFor("something unparseable"));
  }
  @Test void prnHasNoScheduledDoses() {
    assertTrue(MarRules.timesFor("PRN").isEmpty());
    assertTrue(MarRules.timesFor("عند الحاجة").isEmpty());
  }
  @Test void notGivenNeedsReason() {
    assertNull(MarRules.administrationProblem("given", null));
    assertNotNull(MarRules.administrationProblem("refused", null));
    assertNotNull(MarRules.administrationProblem("held", " "));
    assertNull(MarRules.administrationProblem("refused", "المريض رفض الدواء"));
    assertNotNull(MarRules.administrationProblem("vanished", null));
  }
  @Test void doseStatesEscalate() {
    var now = OffsetDateTime.of(2026, 11, 23, 12, 0, 0, 0, ZoneOffset.ofHours(2));
    assertEquals("upcoming", MarRules.doseState(now.plusHours(1), "scheduled", now));
    assertEquals("due", MarRules.doseState(now.minusMinutes(30), "scheduled", now));
    assertEquals("overdue", MarRules.doseState(now.minusMinutes(61), "scheduled", now));
    assertEquals("given", MarRules.doseState(now.minusHours(3), "given", now));
  }
  @Test void painRange() {
    assertNull(MarRules.painProblem(null));
    assertNull(MarRules.painProblem(0));
    assertNull(MarRules.painProblem(10));
    assertNotNull(MarRules.painProblem(11));
    assertNotNull(MarRules.painProblem(-1));
  }
}
