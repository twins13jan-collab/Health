package eg.gov.health.platform.diagnostics;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class RadiologyBoardRulesTest {

  private RadiologyOrder order(String priority) {
    return new RadiologyOrder(UUID.randomUUID(), "CAI", UUID.randomUUID(), null,
        "CT", "chest", "rule out PE", priority, "tester");
  }

  @Test void statOutranksUrgentOutranksRoutine() {
    assertEquals(0, RadiologyBoardRules.priorityRank("stat"));
    assertEquals(0, RadiologyBoardRules.priorityRank("EMERGENCY"));
    assertEquals(1, RadiologyBoardRules.priorityRank("urgent"));
    assertEquals(2, RadiologyBoardRules.priorityRank("routine"));
    assertTrue(RadiologyBoardRules.priorityRank("stat") < RadiologyBoardRules.priorityRank("urgent"));
    assertTrue(RadiologyBoardRules.priorityRank("urgent") < RadiologyBoardRules.priorityRank("routine"));
    assertEquals(3, RadiologyBoardRules.priorityRank(null));
    assertEquals(3, RadiologyBoardRules.priorityRank("whenever"));
  }

  @Test void stagesFollowTheWorklistFlow() {
    assertEquals("acquisition", RadiologyBoardRules.stageOf("ordered"));
    assertEquals("acquisition", RadiologyBoardRules.stageOf("scheduled"));
    assertEquals("acquisition", RadiologyBoardRules.stageOf("in_progress"));
    assertEquals("reporting", RadiologyBoardRules.stageOf("imaged"));
    assertEquals("verification", RadiologyBoardRules.stageOf("reported"));
    assertEquals("done", RadiologyBoardRules.stageOf("verified"));
    assertEquals("done", RadiologyBoardRules.stageOf("cancelled"));
  }

  @Test void reportingSlaDependsOnPriority() {
    assertEquals(60, RadiologyBoardRules.reportingSlaMinutes("stat"));
    assertEquals(240, RadiologyBoardRules.reportingSlaMinutes("urgent"));
    assertEquals(1440, RadiologyBoardRules.reportingSlaMinutes("routine"));
  }

  @Test void overdueOnlyForImagedStudiesPastSla() {
    assertTrue(RadiologyBoardRules.reportOverdue("imaged", "stat", 61));
    assertFalse(RadiologyBoardRules.reportOverdue("imaged", "stat", 60));
    assertFalse(RadiologyBoardRules.reportOverdue("imaged", "routine", 600));
    assertTrue(RadiologyBoardRules.reportOverdue("imaged", "routine", 1441));
    assertFalse(RadiologyBoardRules.reportOverdue("reported", "stat", 99999));
    assertFalse(RadiologyBoardRules.reportOverdue("ordered", "stat", 99999));
  }

  @Test void acquisitionQueuePutsStatFirstEvenWhenOrderedLater() {
    var routine = order("routine");
    var stat = order("stat");
    var urgent = order("urgent");
    var queue = new ArrayList<>(List.of(routine, stat, urgent));
    queue.sort(RadiologyBoardRules.acquisitionOrder());
    assertSame(stat, queue.get(0));
    assertSame(urgent, queue.get(1));
    assertSame(routine, queue.get(2));
  }
}
