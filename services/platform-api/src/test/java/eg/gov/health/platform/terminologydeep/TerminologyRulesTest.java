package eg.gov.health.platform.terminologydeep;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class TerminologyRulesTest {
  @Test void verifiedReviewedMappingsOnly() {
    assertFalse(TerminologyRules.usable(true, false, true));
    assertFalse(TerminologyRules.usable(true, true, false));
    assertFalse(TerminologyRules.usable(false, true, true)); // unsupported stays documented
    assertTrue(TerminologyRules.usable(true, true, true));
  }
}
