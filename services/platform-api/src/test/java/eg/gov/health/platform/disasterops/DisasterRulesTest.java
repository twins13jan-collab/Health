package eg.gov.health.platform.disasterops;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class DisasterRulesTest {
  @Test void activationFollowsClassification() {
    assertTrue(DisasterRules.activationPermitted(2));
    assertFalse(DisasterRules.activationPermitted(null));
    assertFalse(DisasterRules.activationPermitted(5));
  }
  @Test void deliveryFollowsAllocationAndClosureFollowsDelivery() {
    assertFalse(DisasterRules.deliveryPermitted("requested"));
    assertTrue(DisasterRules.deliveryPermitted("allocated"));
    assertFalse(DisasterRules.eventClosable(1));
    assertTrue(DisasterRules.eventClosable(0));
  }
}
