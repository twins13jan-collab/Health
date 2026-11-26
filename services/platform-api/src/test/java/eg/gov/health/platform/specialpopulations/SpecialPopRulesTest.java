package eg.gov.health.platform.specialpopulations;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class SpecialPopRulesTest {
  @Test void detentionNeedsVerifiedSafeguardsEverywhere() {
    assertFalse(SpecialPopRules.pointActivatable("detention", false));
    assertTrue(SpecialPopRules.pointActivatable("detention", true));
    assertTrue(SpecialPopRules.pointActivatable("school", false));
    assertFalse(SpecialPopRules.detentionRecordClosable(true, false)); // rights not explained
    assertTrue(SpecialPopRules.detentionRecordClosable(true, true));
  }
  @Test void unjustifiedBarriersTriggerDenialReview() {
    assertTrue(SpecialPopRules.denialReviewRequired("documents", false));
    assertTrue(SpecialPopRules.denialReviewRequired("fees", false));
    assertFalse(SpecialPopRules.denialReviewRequired("documents", true)); // care was provided anyway
    assertFalse(SpecialPopRules.denialReviewRequired("language", false)); // addressed by interpreters, not denial
  }
}
