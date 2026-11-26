package eg.gov.health.platform.cssd;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class CssdBoardRulesTest {

  private final LocalDate today = LocalDate.of(2026, 11, 23);

  @Test void expiryStatesForSterileStock() {
    assertEquals("expired", CssdBoardRules.expiryState("sterile", LocalDate.of(2026, 11, 22), today));
    assertEquals("expiring_soon", CssdBoardRules.expiryState("sterile", LocalDate.of(2026, 11, 30), today));
    assertEquals("expiring_soon", CssdBoardRules.expiryState("sterile", today, today));
    assertEquals("valid", CssdBoardRules.expiryState("sterile", LocalDate.of(2026, 12, 15), today));
    assertEquals("na", CssdBoardRules.expiryState("in_process", LocalDate.of(2026, 11, 22), today));
    assertEquals("na", CssdBoardRules.expiryState("sterile", null, today));
  }

  @Test void sterilizerDueStates() {
    assertEquals("overdue", CssdBoardRules.dueState(LocalDate.of(2026, 11, 1), today));
    assertEquals("due_soon", CssdBoardRules.dueState(LocalDate.of(2026, 12, 20), today));
    assertEquals("ok", CssdBoardRules.dueState(LocalDate.of(2027, 3, 1), today));
    assertEquals("unknown", CssdBoardRules.dueState(null, today));
  }

  @Test void missingStepsAreNamedExactly() {
    assertTrue(CssdBoardRules.missingSteps(true, true, true, true, "فني التعقيم").isEmpty());
    var m = CssdBoardRules.missingSteps(true, false, false, true, null);
    assertEquals(java.util.List.of("disinfection", "drying", "packing"), m);
    assertEquals(5, CssdBoardRules.missingSteps(false, false, false, false, " ").size());
  }

  @Test void attentionRankPutsRecallsAndQuarantinesFirst() {
    assertEquals(0, CssdBoardRules.setAttentionRank("recalled", "valid"));
    assertEquals(1, CssdBoardRules.setAttentionRank("quarantined", "na"));
    assertEquals(2, CssdBoardRules.setAttentionRank("sterile", "expired"));
    assertEquals(3, CssdBoardRules.setAttentionRank("sterile", "expiring_soon"));
    assertEquals(4, CssdBoardRules.setAttentionRank("in_process", "na"));
    assertEquals(5, CssdBoardRules.setAttentionRank("sterile", "valid"));
    assertTrue(CssdBoardRules.setAttentionRank("recalled", "na") < CssdBoardRules.setAttentionRank("sterile", "expired"));
  }

  @Test void warnWindowsAreDepartmentPolicy() {
    assertEquals(7, CssdBoardRules.EXPIRY_WARN_DAYS);
    assertEquals(30, CssdBoardRules.STERILIZER_DUE_WARN_DAYS);
    // boundary: exactly at the warn horizon counts as expiring soon / due soon
    assertEquals("expiring_soon", CssdBoardRules.expiryState("sterile", today.plusDays(7), today));
    assertEquals("valid", CssdBoardRules.expiryState("sterile", today.plusDays(8), today));
    assertEquals("due_soon", CssdBoardRules.dueState(today.plusDays(30), today));
    assertEquals("ok", CssdBoardRules.dueState(today.plusDays(31), today));
  }
}
