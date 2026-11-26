package eg.gov.health.platform.digitalquality;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class DigitalQualityRulesTest {
  @Test void qualityGateIsMeasured() {
    assertTrue(DigitalQualityRules.checkPasses(94.0, 1200, true));
    assertFalse(DigitalQualityRules.checkPasses(72.0, 1200, true));  // tasks failing
    assertFalse(DigitalQualityRules.checkPasses(94.0, 5200, true));  // too slow
    assertFalse(DigitalQualityRules.checkPasses(94.0, 1200, false)); // API link never verified against real data
  }
  @Test void demoDataOnProductionIsNonCompliance() {
    assertFalse(DigitalQualityRules.surfaceCompliant("production", 1, true));
    assertTrue(DigitalQualityRules.surfaceCompliant("production", 0, true));
    assertTrue(DigitalQualityRules.surfaceCompliant("demo", 1, true)); // demo belongs in the demo environment
    assertFalse(DigitalQualityRules.surfaceCompliant("production", 0, false));
  }
}
