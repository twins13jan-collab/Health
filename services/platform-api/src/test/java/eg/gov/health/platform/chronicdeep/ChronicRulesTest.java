package eg.gov.health.platform.chronicdeep;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ChronicRulesTest {
  @Test void declaredCriteriaAndVerifiedSources() {
    assertFalse(ChronicRules.enrollable(null, true));
    assertFalse(ChronicRules.enrollable("معايير السكري 2026", false));
    assertTrue(ChronicRules.enrollable("معايير السكري 2026", true));
    assertFalse(ChronicRules.countsTowardControl(false));
    assertTrue(ChronicRules.controlled(118.0, 130.0));
    assertFalse(ChronicRules.controlled(165.0, 130.0));
  }
}
