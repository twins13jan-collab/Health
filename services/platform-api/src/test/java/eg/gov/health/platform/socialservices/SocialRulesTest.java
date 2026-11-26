package eg.gov.health.platform.socialservices;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class SocialRulesTest {
  @Test void crisisAndHighRiskNeedSafetyPlans() {
    assertTrue(SocialRules.safetyPlanRequired("crisis"));
    assertTrue(SocialRules.safetyPlanRequired("high"));
    assertFalse(SocialRules.safetyPlanRequired("moderate"));
  }
  @Test void planCannotCloseWithDutiesOutstanding() {
    assertFalse(SocialRules.planClosable("crisis", false, "none", true));  // no safety plan
    assertTrue(SocialRules.planClosable("crisis", true, "none", false));
    assertFalse(SocialRules.planClosable("moderate", true, "child", false)); // safeguarding unmade
    assertTrue(SocialRules.planClosable("moderate", true, "child", true));
    assertTrue(SocialRules.planClosable("none", false, "none", false));
  }
}
