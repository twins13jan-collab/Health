package eg.gov.health.platform.labsafety;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
class LabSafetyRulesTest {
  @Test void introductionNeedsFullVerification() {
    assertTrue(LabSafetyRules.introductionApprovable(true, true, true, true));
    assertFalse(LabSafetyRules.introductionApprovable(true, true, true, false)); // interferences unchecked
    assertFalse(LabSafetyRules.introductionApprovable(false, true, true, true)); // no risk assessment
  }
  @Test void expiredLotsAndUnassessedFailuresAreBlocked() {
    assertTrue(LabSafetyRules.lotUsable(LocalDate.parse("2027-01-01"), LocalDate.parse("2026-11-22")));
    assertFalse(LabSafetyRules.lotUsable(LocalDate.parse("2026-01-01"), LocalDate.parse("2026-11-22")));
    assertFalse(LabSafetyRules.returnToServicePermitted(false));
    assertTrue(LabSafetyRules.returnToServicePermitted(true));
    assertTrue(LabSafetyRules.comparisonAcceptable(6.5));
    assertFalse(LabSafetyRules.comparisonAcceptable(-14.0));
  }
}
