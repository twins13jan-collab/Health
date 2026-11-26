package eg.gov.health.platform.criticalcare;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class CriticalCareRulesTest {
  @Test void allFourGatesForACriticalMove() {
    assertTrue(CriticalCareRules.transferBlockers(true, true, true, true).isEmpty());
    assertTrue(CriticalCareRules.transferBlockers(true, false, true, true).contains("stability_unreviewed"));
    assertTrue(CriticalCareRules.transferBlockers(true, true, false, true).contains("team_or_equipment_unfit"));
    assertEquals(4, CriticalCareRules.transferBlockers(false, false, false, false).size());
  }
}
