package eg.gov.health.platform.eligibility;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class EligibilityRulesTest {
  @Test void everyFailedCheckIsNamed() {
    var r = EligibilityRules.restrictions(true, true, false, true, false, true, true);
    assertEquals(List.of("no_capacity", "equipment_unavailable"), r);
    assertEquals("ineligible", EligibilityRules.decide(r, true));
  }
  @Test void preparationIsAConditionNotABlock() {
    assertEquals("eligible", EligibilityRules.decide(List.of(), true));
    assertEquals("eligible_with_conditions", EligibilityRules.decide(List.of(), false));
  }
  @Test void clinicalIneligibilityBlocks() {
    var r = EligibilityRules.restrictions(true, true, true, true, true, true, false);
    assertEquals("ineligible", EligibilityRules.decide(r, true));
  }
  @Test void coverageNeverDecides() {
    // decide() has no coverage parameter by design; the financial note only reports.
    assertTrue(EligibilityRules.financialNote("not_covered").contains("clinical decision stands"));
    assertTrue(EligibilityRules.financialNote("preauth_required").contains("not a clinical barrier"));
  }
}
