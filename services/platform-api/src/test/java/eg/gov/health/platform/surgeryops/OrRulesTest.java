package eg.gov.health.platform.surgeryops;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class OrRulesTest {
  @Test void priorityOrder() {
    assertEquals(0, OrRules.priorityRank("emergency"));
    assertEquals(1, OrRules.priorityRank("urgent"));
    assertEquals(2, OrRules.priorityRank("elective"));
    assertEquals(2, OrRules.priorityRank(null));
  }
  @Test void roomSeparationWindow() {
    var t = OffsetDateTime.of(2026, 11, 23, 9, 0, 0, 0, ZoneOffset.ofHours(2));
    assertTrue(OrRules.roomConflict(t.plusMinutes(30), List.of(t)));
    assertTrue(OrRules.roomConflict(t.minusMinutes(59), List.of(t)));
    assertFalse(OrRules.roomConflict(t.plusMinutes(60), List.of(t)));
    assertFalse(OrRules.roomConflict(t.plusHours(3), List.of(t)));
    assertFalse(OrRules.roomConflict(null, List.of(t)));
  }
  @Test void anaesthesiaValidation() {
    assertNull(OrRules.anaesthesiaProblem(3, "general"));
    assertNotNull(OrRules.anaesthesiaProblem(6, "general"));
    assertNotNull(OrRules.anaesthesiaProblem(0, null));
    assertNotNull(OrRules.anaesthesiaProblem(2, "magic"));
    assertNull(OrRules.anaesthesiaProblem(null, null));
  }
  @Test void startGates() {
    assertNull(OrRules.startProblem("scheduled", true, true, "elective"));
    assertNotNull(OrRules.startProblem("scheduled", false, false, "elective"));
    assertNotNull(OrRules.startProblem("scheduled", true, false, "elective"));
    assertNotNull(OrRules.startProblem("scheduled", true, false, "urgent"));
    assertNull(OrRules.startProblem("scheduled", true, false, "emergency"));
    assertNotNull(OrRules.startProblem("completed", true, true, "elective"));
  }
  @Test void reasonRequired() {
    assertNotNull(OrRules.reasonProblem(null));
    assertNotNull(OrRules.reasonProblem(" "));
    assertNull(OrRules.reasonProblem("تأجيل لعدم توفر سرير عناية"));
  }
}
