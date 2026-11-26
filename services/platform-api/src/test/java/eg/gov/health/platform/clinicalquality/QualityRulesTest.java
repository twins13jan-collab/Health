package eg.gov.health.platform.clinicalquality;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class QualityRulesTest {
  @Test void adjustmentAndVerifiedEffectiveness() {
    assertFalse(QualityRules.comparable(false));
    assertTrue(QualityRules.comparable(true));
    assertFalse(QualityRules.planClosable(5, 5, false)); // done but unproven
    assertFalse(QualityRules.planClosable(5, 3, true));
    assertTrue(QualityRules.planClosable(5, 5, true));
  }
}
