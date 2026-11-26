package eg.gov.health.platform.rehab;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class RehabRulesTest {
  @Test void sessionsNeedBaselineAndApprovedPlan() {
    assertFalse(RehabRules.sessionsAllowed(false, "approved"));
    assertFalse(RehabRules.sessionsAllowed(true, "draft"));
    assertTrue(RehabRules.sessionsAllowed(true, "approved"));
    assertTrue(RehabRules.sessionsAllowed(true, "active"));
  }
  @Test void acutePainOrDeteriorationStopsSession() {
    assertTrue(RehabRules.mustStopAndEscalate(8, "none"));
    assertTrue(RehabRules.mustStopAndEscalate(3, "deterioration"));
    assertTrue(RehabRules.mustStopAndEscalate(3, "fall"));
    assertFalse(RehabRules.mustStopAndEscalate(5, "none"));
  }
  @Test void measuredGoalNotSessionCount() {
    assertTrue(RehabRules.goalMet(40, 60, 62, true));
    assertFalse(RehabRules.goalMet(40, 60, 55, true));
    assertEquals("goal_met", RehabRules.progressVerdict(40.0, 62.0, 60, 12));
    assertEquals("improving_not_yet_goal", RehabRules.progressVerdict(40.0, 50.0, 60, 12));
    // 30 sessions but no measurement -> cannot claim improvement
    assertEquals("insufficient_measurement", RehabRules.progressVerdict(40.0, null, 60, 30));
    assertEquals("declining_review_plan", RehabRules.progressVerdict(40.0, 35.0, 60, 4));
  }
  @Test void missedNeedsReasonAndOutcomeNeedsVersion() {
    assertFalse(RehabRules.missedReasonValid(" "));
    assertTrue(RehabRules.missedReasonValid("patient fever"));
    assertFalse(RehabRules.outcomeValid("ADL", null));
    assertTrue(RehabRules.outcomeValid("ADL", "v2"));
  }
}
