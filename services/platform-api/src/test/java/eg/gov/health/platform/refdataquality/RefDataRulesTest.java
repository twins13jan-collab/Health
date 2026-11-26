package eg.gov.health.platform.refdataquality;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class RefDataRulesTest {
  @Test void sensitiveMatchesNeedHumanEyes() {
    assertFalse(RefDataRules.activatable(true, false));
    assertTrue(RefDataRules.activatable(true, true));
    assertTrue(RefDataRules.activatable(false, false));
  }
}
