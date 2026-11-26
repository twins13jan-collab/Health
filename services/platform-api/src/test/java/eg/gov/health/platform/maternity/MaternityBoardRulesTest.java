package eg.gov.health.platform.maternity;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class MaternityBoardRulesTest {

  @Test void overdueOnlyWhileUndelivered() {
    var edd = LocalDate.of(2026, 11, 20);
    assertTrue(MaternityBoardRules.isOverdue(edd, LocalDate.of(2026, 11, 23), "ongoing"));
    assertTrue(MaternityBoardRules.isOverdue(edd, LocalDate.of(2026, 11, 23), "in_labor"));
    assertFalse(MaternityBoardRules.isOverdue(edd, LocalDate.of(2026, 11, 23), "delivered"));
    assertFalse(MaternityBoardRules.isOverdue(edd, LocalDate.of(2026, 11, 23), "postpartum"));
  }

  @Test void notOverdueBeforeOrOnEdd() {
    var edd = LocalDate.of(2026, 11, 23);
    assertFalse(MaternityBoardRules.isOverdue(edd, LocalDate.of(2026, 11, 23), "ongoing"));
    assertFalse(MaternityBoardRules.isOverdue(edd, LocalDate.of(2026, 11, 20), "ongoing"));
    assertFalse(MaternityBoardRules.isOverdue(null, LocalDate.of(2026, 11, 23), "ongoing"));
  }

  @Test void pphEmergencyOutranksLabour() {
    assertEquals(0, MaternityBoardRules.attentionRank(true, false, "low", false));
    assertEquals(0, MaternityBoardRules.attentionRank(true, true, "critical", true));
    assertTrue(MaternityBoardRules.attentionRank(true, false, "low", false)
      < MaternityBoardRules.attentionRank(false, true, "low", false));
  }

  @Test void labourOutranksCriticalAndOverdue() {
    assertEquals(1, MaternityBoardRules.attentionRank(false, true, "low", false));
    assertTrue(MaternityBoardRules.attentionRank(false, true, "low", false)
      < MaternityBoardRules.attentionRank(false, false, "critical", false));
    assertTrue(MaternityBoardRules.attentionRank(false, false, "critical", false)
      < MaternityBoardRules.attentionRank(false, false, "low", true));
  }

  @Test void overdueOutranksHighRiskAndRoutine() {
    assertTrue(MaternityBoardRules.attentionRank(false, false, "low", true)
      < MaternityBoardRules.attentionRank(false, false, "high", false));
    assertEquals(5, MaternityBoardRules.attentionRank(false, false, "low", false));
    assertEquals(5, MaternityBoardRules.attentionRank(false, false, null, false));
  }
}
