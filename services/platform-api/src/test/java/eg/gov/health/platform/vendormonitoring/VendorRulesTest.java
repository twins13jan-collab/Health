package eg.gov.health.platform.vendormonitoring;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class VendorRulesTest {
  @Test void measuredStandingAndReviewedExceptions() {
    assertFalse(VendorRules.atRisk(96.0, 88.0));
    assertTrue(VendorRules.atRisk(82.0, 88.0));
    assertTrue(VendorRules.atRisk(96.0, 71.0));
    assertFalse(VendorRules.exceptionalLegitimate(true, false));
    assertTrue(VendorRules.exceptionalLegitimate(true, true));
  }
}
