package eg.gov.health.platform.preventive;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class PreventiveRulesTest {
  @Test void screeningNeedsEligibilityAndConsent() {
    assertTrue(PreventiveRules.screeningPermitted(true, true));
    assertFalse(PreventiveRules.screeningPermitted(false, true));
    assertFalse(PreventiveRules.screeningPermitted(true, false));
  }
  @Test void positiveScreenClosesOnlyThroughConfirmationAndReferral() {
    assertFalse(PreventiveRules.pathwayClosable("positive", false, false));
    assertFalse(PreventiveRules.pathwayClosable("positive", true, false));
    assertTrue(PreventiveRules.pathwayClosable("positive", true, true));
    assertTrue(PreventiveRules.pathwayClosable("negative", false, false));
  }
  @Test void coverageIsHonestArithmetic() {
    assertEquals(62.5, PreventiveRules.coverage(50, 80));
    assertEquals(0.0, PreventiveRules.coverage(0, 0));
  }
}
