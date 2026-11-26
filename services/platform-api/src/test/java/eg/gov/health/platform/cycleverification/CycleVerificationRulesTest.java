package eg.gov.health.platform.cycleverification;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class CycleVerificationRulesTest {
  @Test void everyStageEvidenced() {
    assertTrue(CycleVerificationRules.complete(true, true, true, true, true, true, "أدلة-1..6"));
    assertFalse(CycleVerificationRules.complete(true, true, true, true, true, false, "أدلة"));
    assertFalse(CycleVerificationRules.complete(true, true, true, true, true, true, " ")); // stages without evidence
    assertTrue(CycleVerificationRules.missingStages(true, true, false, true, true, true).contains("approval"));
  }
}
