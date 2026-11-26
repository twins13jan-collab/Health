package eg.gov.health.platform.meddelivery;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class DeliveryRulesTest {
  @Test void dispatchNeedsEligibilityApprovalAndPreparation() {
    assertTrue(DeliveryRules.dispatchPermitted(true, true, "prepared"));
    assertFalse(DeliveryRules.dispatchPermitted(false, true, "prepared")); // eligibility unverified
    assertFalse(DeliveryRules.dispatchPermitted(true, false, "prepared")); // pharmacist has not approved
    assertFalse(DeliveryRules.dispatchPermitted(true, true, "reviewed"));  // not yet prepared
  }
  @Test void specialHandoverNeedsIdentityAndIntactColdChain() {
    assertFalse(DeliveryRules.handoverValid(true, false, true, false));  // controlled med, no identity check
    assertTrue(DeliveryRules.handoverValid(true, true, true, false));
    assertFalse(DeliveryRules.handoverValid(false, true, false, true));  // cold-chain breach voids delivery
    assertTrue(DeliveryRules.handoverValid(false, false, true, false));  // ordinary med, ordinary handover
  }
  @Test void returnsAreManagedNotImprovised() {
    assertFalse(DeliveryRules.returnRequired(1));
    assertTrue(DeliveryRules.returnRequired(2));
    assertEquals("quarantine", DeliveryRules.returnDisposition(true, false)); // breached pack never restocks
    assertEquals("restock", DeliveryRules.returnDisposition(true, true));
  }
}
