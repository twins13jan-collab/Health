package eg.gov.health.platform.nutrition;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class NutritionRulesTest {
  @Test void bmiAndRisk() {
    assertEquals(17.3, NutritionRules.bmi(50.0, 170.0));
    assertEquals(24.2, NutritionRules.bmi(70.0, 170.0));
    assertEquals("high", NutritionRules.riskLevel(17.3, 2.0));     // low BMI
    assertEquals("high", NutritionRules.riskLevel(24.0, 12.0));    // >10% loss
    assertEquals("moderate", NutritionRules.riskLevel(19.5, 2.0));
    assertEquals("moderate", NutritionRules.riskLevel(24.0, 7.0)); // >5% loss
    assertEquals("low", NutritionRules.riskLevel(24.2, 1.0));
  }
  @Test void needsRange() {
    int[] r = NutritionRules.needsRangeKcal(70.0);
    assertEquals(1750, r[0]); assertEquals(2100, r[1]);
    assertFalse(NutritionRules.needsOutOfRange(1900, 70.0));
    assertTrue(NutritionRules.needsOutOfRange(3200, 70.0));
  }
  @Test void approvalAndReviewGates() {
    assertFalse(NutritionRules.ordersAllowed("proposed"));
    assertTrue(NutritionRules.ordersAllowed("approved"));
    assertTrue(NutritionRules.mayStart("enteral", false));
    assertFalse(NutritionRules.mayStart("parenteral", false));
    assertTrue(NutritionRules.mayStart("parenteral", true));
  }
  @Test void gapNeedsReason() {
    assertTrue(NutritionRules.gapNeedsReason(1000, 600, false, null));   // shortfall, no reason
    assertFalse(NutritionRules.gapNeedsReason(1000, 600, false, "NPO for procedure"));
    assertFalse(NutritionRules.gapNeedsReason(1000, 1000, false, null)); // full delivery
    assertTrue(NutritionRules.gapNeedsReason(1000, 1000, true, " "));    // stopped early, blank reason
    assertEquals(400.0, NutritionRules.varianceMl(1000, 600));
  }
  @Test void aspirationAndIntoleranceHoldFeed() {
    assertTrue(NutritionRules.holdsFeed("aspiration"));
    assertTrue(NutritionRules.holdsFeed("intolerance"));
    assertFalse(NutritionRules.holdsFeed("metabolic"));
  }
}
