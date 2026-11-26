package eg.gov.health.platform.continuity;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
class ContinuityRulesTest {
  @Test void restoreProofMeansMeetingTargets() {
    assertTrue(ContinuityRules.restoreTestPassed(45, 10, 60, 15, true));
    assertFalse(ContinuityRules.restoreTestPassed(90, 10, 60, 15, true));  // RTO missed
    assertFalse(ContinuityRules.restoreTestPassed(45, 30, 60, 15, true));  // RPO missed
    assertFalse(ContinuityRules.restoreTestPassed(45, 10, 60, 15, false)); // restore failed
  }
  @Test void proofExpiresAfterSixMonths() {
    var today = LocalDate.parse("2026-11-22");
    assertTrue(ContinuityRules.recoveryProven(LocalDate.parse("2026-08-01"), today));
    assertFalse(ContinuityRules.recoveryProven(LocalDate.parse("2026-01-01"), today));
    assertFalse(ContinuityRules.recoveryProven(null, today));
  }
}
