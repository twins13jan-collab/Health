package eg.gov.health.platform.endoscopy;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class EndoscopyRulesTest {
  @Test void reprocessingNeedsAllThreeSteps() {
    assertTrue(EndoscopyRules.reprocessingPassed(true, true, true));
    assertFalse(EndoscopyRules.reprocessingPassed(true, true, false)); // leak test failed
    assertFalse(EndoscopyRules.reprocessingPassed(false, true, true));
  }
  @Test void scopeUsableOnlyAvailableAndPassed() {
    assertTrue(EndoscopyRules.scopeUsable("available", "passed"));
    assertFalse(EndoscopyRules.scopeUsable("quarantined", "passed"));
    assertFalse(EndoscopyRules.scopeUsable("available", "failed"));
    assertFalse(EndoscopyRules.scopeUsable("available", "none"));
  }
  @Test void checklistAllFour() {
    assertTrue(EndoscopyRules.checklistComplete(true, true, true, true));
    assertFalse(EndoscopyRules.checklistComplete(true, true, true, false)); // no consent
    assertFalse(EndoscopyRules.checklistComplete(false, true, true, true)); // not fasting
  }
  @Test void dischargeByAldreteOrDecision() {
    assertTrue(EndoscopyRules.dischargeable(9, null));
    assertFalse(EndoscopyRules.dischargeable(7, null));
    assertTrue(EndoscopyRules.dischargeable(7, "قرار طبيب موثق بمرافقة ومتابعة"));
  }
}
