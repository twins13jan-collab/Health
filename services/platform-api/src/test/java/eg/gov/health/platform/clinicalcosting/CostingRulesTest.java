package eg.gov.health.platform.clinicalcosting;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class CostingRulesTest {
  @Test void varianceBeyondQuarterOpensReview() {
    assertEquals(40.0, CostingRules.variancePct(14000, 10000.0));
    assertTrue(CostingRules.varianceReviewNeeded(14000, 10000.0));
    assertFalse(CostingRules.varianceReviewNeeded(11000, 10000.0));
    assertTrue(CostingRules.varianceReviewNeeded(6000, 10000.0)); // deep under-run is variance too
  }
  @Test void financeAloneNeverClosesAClinicalVarianceReview() {
    assertFalse(CostingRules.mayCloseVarianceReview("finance_manager"));
    assertTrue(CostingRules.mayCloseVarianceReview("clinical_director"));
    assertTrue(CostingRules.mayCloseVarianceReview("admin"));
  }
}
