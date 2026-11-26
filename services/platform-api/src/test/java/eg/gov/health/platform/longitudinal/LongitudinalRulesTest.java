package eg.gov.health.platform.longitudinal;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class LongitudinalRulesTest {
  @Test void registrarCannotDecide() {
    assertFalse(LongitudinalRules.mayDecideMerge("receptionist"));
    assertFalse(LongitudinalRules.mayDecideMerge("registrar"));
    assertTrue(LongitudinalRules.mayDecideMerge("identity_quality_officer"));
  }
  @Test void highRiskNeedsDualApproval() {
    assertTrue(LongitudinalRules.highRisk(0.75, "temporary", "temporary")); // uncertain match
    assertTrue(LongitudinalRules.highRisk(0.99, "verified", "temporary"));  // verified involved
    assertFalse(LongitudinalRules.highRisk(0.99, "temporary", "temporary"));
    assertFalse(LongitudinalRules.mergeApprovable(true, true, "officer@x", null));
    assertFalse(LongitudinalRules.mergeApprovable(true, true, "officer@x", "officer@x")); // same person
    assertTrue(LongitudinalRules.mergeApprovable(true, true, "officer@x", "second@x"));
    assertTrue(LongitudinalRules.mergeApprovable(true, false, "officer@x", null));
    assertFalse(LongitudinalRules.mergeApprovable(false, false, "officer@x", "second@x"));
  }
  @Test void evidenceForImperfectMatches() {
    assertTrue(LongitudinalRules.evidenceRequired(0.95));
    assertFalse(LongitudinalRules.evidenceRequired(1.0));
  }
}
