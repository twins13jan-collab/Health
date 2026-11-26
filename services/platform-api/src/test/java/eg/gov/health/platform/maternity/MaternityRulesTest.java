package eg.gov.health.platform.maternity;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
class MaternityRulesTest {
  @Test void eddByNaegele() {
    assertEquals(LocalDate.of(2027,1,8), MaternityRules.edd(LocalDate.of(2026,4,3)));
    assertNull(MaternityRules.edd(null));
  }
  @Test void gestationalAgeWeeksDays() {
    var ga = MaternityRules.gestationalAge(LocalDate.of(2026,1,1), LocalDate.of(2026,3,5));
    assertEquals(9, ga.weeks()); assertEquals(0, ga.days());
    assertThrows(IllegalArgumentException.class, () -> MaternityRules.gestationalAge(LocalDate.of(2026,3,5), LocalDate.of(2026,1,1)));
  }
  @Test void riskSuggestionBands() {
    assertEquals("critical", MaternityRules.riskSuggestion(170, 100, false, false, false, 1, 28, false, false));
    assertEquals("critical", MaternityRules.riskSuggestion(120, 80, false, true, false, 1, 28, false, false));
    assertEquals("high", MaternityRules.riskSuggestion(145, 95, false, false, false, 1, 28, false, false));
    assertEquals("high", MaternityRules.riskSuggestion(120, 80, false, false, false, 2, 28, false, false));
    assertEquals("low", MaternityRules.riskSuggestion(120, 80, false, false, false, 1, 28, false, false));
  }
  @Test void pphThresholdsByDeliveryType() {
    assertTrue(MaternityRules.isPph(500, "vaginal"));
    assertFalse(MaternityRules.isPph(499, "vaginal"));
    assertFalse(MaternityRules.isPph(999, "cesarean"));
    assertTrue(MaternityRules.isPph(1000, "cesarean"));
  }
  @Test void dischargeBlockedByDangerSigns() {
    assertTrue(MaternityRules.dischargeBlocked("heavy", 120, 80, 3, "stable"));
    assertTrue(MaternityRules.dischargeBlocked("normal", 150, 95, 3, "stable"));
    assertFalse(MaternityRules.dischargeBlocked("normal", 120, 80, 3, "stable"));
  }
}
