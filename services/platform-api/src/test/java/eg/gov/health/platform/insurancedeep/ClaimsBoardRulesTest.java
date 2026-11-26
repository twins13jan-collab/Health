package eg.gov.health.platform.insurancedeep;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ClaimsBoardRulesTest {
  @Test void gapsAreNamed() {
    assertEquals(java.util.List.of("documentation_incomplete", "tariff_mismatch"),
        ClaimsBoardRules.gaps(false, false));
    assertEquals(java.util.List.of("tariff_mismatch"), ClaimsBoardRules.gaps(true, false));
    assertTrue(ClaimsBoardRules.gaps(true, true).isEmpty());
  }
  @Test void unreviewedFraudBlocksAndTops() {
    String st = ClaimsBoardRules.claimState(true, false, false, true, true, false);
    assertEquals("fraud_review_pending", st);
    assertEquals(0, ClaimsBoardRules.claimRank(st));
  }
  @Test void reviewedFraudFallsThroughToRealState() {
    assertEquals("awaiting_settlement",
        ClaimsBoardRules.claimState(true, true, false, true, true, false));
    assertEquals("awaiting_decision",
        ClaimsBoardRules.claimState(false, false, false, true, false, false));
  }
  @Test void appealsOutrankAdjudicationGaps() {
    assertEquals("appeal_open", ClaimsBoardRules.claimState(false, false, true, false, false, false));
    assertTrue(ClaimsBoardRules.claimRank("appeal_open") < ClaimsBoardRules.claimRank("not_adjudicable"));
  }
  @Test void settledLeavesTheQueue() {
    assertEquals("settled", ClaimsBoardRules.claimState(true, false, true, false, false, true));
    assertEquals(9, ClaimsBoardRules.claimRank("settled"));
  }
}
