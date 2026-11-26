package eg.gov.health.platform.painsedation;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class SedationRulesTest {
  @Test void painScaleMustFitThePatient() {
    assertTrue(SedationRules.scaleAppropriate("FLACC", "preverbal"));
    assertFalse(SedationRules.scaleAppropriate("NRS", "preverbal")); // a baby cannot report a number
    assertTrue(SedationRules.scaleAppropriate("WONGBAKER", "child"));
    assertTrue(SedationRules.scaleAppropriate("NRS", "adult"));
    assertFalse(SedationRules.scaleAppropriate("FLACC", "adult"));
  }
  @Test void sedationNeedsAssessmentAirwayAndConsent() {
    assertTrue(SedationRules.sedationStartPermitted(true, true, true));
    assertFalse(SedationRules.sedationStartPermitted(true, false, true));
    assertFalse(SedationRules.sedationStartPermitted(false, true, true));
    assertFalse(SedationRules.sedationStartPermitted(true, true, false));
  }
  @Test void dischargeIsEarnedAgainstRecoveryCriteria() {
    assertTrue(SedationRules.dischargeSafe(9));
    assertTrue(SedationRules.dischargeSafe(10));
    assertFalse(SedationRules.dischargeSafe(7));
    assertFalse(SedationRules.dischargeSafe(null));
  }
}
