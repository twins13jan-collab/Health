package eg.gov.health.platform.uxgov;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class UxRulesTest {
  @Test void criticalTasksCarryASpeedTarget() {
    assertTrue(UxRules.criticalTaskFastEnough(true, 60, 45));
    assertFalse(UxRules.criticalTaskFastEnough(true, 60, 95)); // slow critical task is a safety finding
    assertTrue(UxRules.criticalTaskFastEnough(false, null, 9999)); // non-critical tasks are not gated
  }
  @Test void validationNeedsACompletedSessionAndNoOpenBlockers() {
    assertTrue(UxRules.scenarioValidatable(true, 0));
    assertFalse(UxRules.scenarioValidatable(true, 1));  // open blocker blocks sign-off
    assertFalse(UxRules.scenarioValidatable(false, 0)); // untested is unvalidated
  }
}
