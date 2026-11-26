package eg.gov.health.platform.amrdeep;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class AmrRulesTest {
  @Test void standardizedRatesAndVerifiedPatterns() {
    assertEquals(40.0, AmrRules.resistanceRate(40, 100));
    assertEquals(-1.0, AmrRules.resistanceRate(0, 0));
    assertTrue(AmrRules.unusualPattern(55.0, 30.0));   // +25 points
    assertFalse(AmrRules.unusualPattern(45.0, 30.0));  // +15 points
    assertFalse(AmrRules.publishable(false, false, false)); // identity exposed
    assertFalse(AmrRules.publishable(true, true, false));   // unusual, unverified
    assertTrue(AmrRules.publishable(true, true, true));
    assertTrue(AmrRules.publishable(true, false, false));
  }
}
