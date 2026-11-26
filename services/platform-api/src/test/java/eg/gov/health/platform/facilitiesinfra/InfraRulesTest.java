package eg.gov.health.platform.facilitiesinfra;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class InfraRulesTest {
  @Test void criticalRepairsNeedFallbackAndProof() {
    assertFalse(InfraRules.workOrderClosable(true, "critical", null, true));        // no fallback
    assertFalse(InfraRules.workOrderClosable(true, "critical", "أسطوانات احتياطية", false)); // still out of limits
    assertTrue(InfraRules.workOrderClosable(true, "critical", "أسطوانات احتياطية", true));
    assertTrue(InfraRules.workOrderClosable(false, "routine", null, false));        // routine work is routine
  }
}
