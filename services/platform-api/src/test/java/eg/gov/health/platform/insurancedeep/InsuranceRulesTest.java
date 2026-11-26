package eg.gov.health.platform.insurancedeep;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class InsuranceRulesTest {
  @Test void matchingAndHumanFraudReview() {
    assertFalse(InsuranceRules.adjudicable(true, false));
    assertTrue(InsuranceRules.adjudicable(true, true));
    assertFalse(InsuranceRules.settleable(true, false));  // flagged, unreviewed
    assertTrue(InsuranceRules.settleable(true, true));
    assertTrue(InsuranceRules.settleable(false, false));
  }
}
