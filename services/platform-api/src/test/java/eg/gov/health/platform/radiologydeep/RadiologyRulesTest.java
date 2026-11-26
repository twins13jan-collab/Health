package eg.gov.health.platform.radiologydeep;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class RadiologyRulesTest {
  @Test void contrastIsAKidneyDecision() {
    assertFalse(RadiologyRules.contrastPermitted(false, true, 80.0, false)); // contraindications unchecked
    assertFalse(RadiologyRules.contrastPermitted(true, true, null, false));  // no eGFR recorded
    assertFalse(RadiologyRules.contrastPermitted(true, true, 22.0, false));  // eGFR < 30 without override
    assertTrue(RadiologyRules.contrastPermitted(true, true, 22.0, true));
    assertTrue(RadiologyRules.contrastPermitted(true, true, 80.0, false));
    assertTrue(RadiologyRules.contrastPermitted(false, false, null, false)); // no contrast planned
    assertFalse(RadiologyRules.orderClosable(true, false));
    assertTrue(RadiologyRules.orderClosable(true, true));
    assertTrue(RadiologyRules.orderClosable(false, false));
  }
}
