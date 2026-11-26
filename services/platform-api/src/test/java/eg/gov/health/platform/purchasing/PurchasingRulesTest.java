package eg.gov.health.platform.purchasing;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
class PurchasingRulesTest {
  @Test void separationOfDuties() {
    assertFalse(PurchasingRules.approvable("req@x", "req@x")); // requester never self-approves
    assertTrue(PurchasingRules.approvable("req@x", "mgr@x"));
    assertFalse(PurchasingRules.receiverIndependent("issuer@x", "issuer@x"));
    assertTrue(PurchasingRules.receiverIndependent("issuer@x", "store@x"));
    assertFalse(PurchasingRules.payerIndependent("issuer@x", "store@x", "issuer@x"));
    assertFalse(PurchasingRules.payerIndependent("issuer@x", "store@x", "store@x"));
    assertTrue(PurchasingRules.payerIndependent("issuer@x", "store@x", "finance@x"));
  }
  @Test void partialOrSubstituteNeedsBothApprovals() {
    assertTrue(PurchasingRules.deliveryApprovable(false, false, null, null));
    assertFalse(PurchasingRules.deliveryApprovable(true, false, "فني", null));
    assertFalse(PurchasingRules.deliveryApprovable(false, true, null, "تعاقدي"));
    assertTrue(PurchasingRules.deliveryApprovable(true, false, "فني", "تعاقدي"));
  }
  @Test void threeWayMatch() {
    assertTrue(PurchasingRules.threeWayMatch(new BigDecimal("1000"), new BigDecimal("1000.00"), "accepted"));
    assertFalse(PurchasingRules.threeWayMatch(new BigDecimal("1000"), new BigDecimal("999"), "accepted"));
    assertFalse(PurchasingRules.threeWayMatch(new BigDecimal("1000"), new BigDecimal("1000"), "rejected"));
    assertFalse(PurchasingRules.threeWayMatch(new BigDecimal("1000"), new BigDecimal("1000"), null));
  }
  @Test void bankChangeNeedsIndependentVerifier() {
    assertFalse(PurchasingRules.bankChangeVerified("a@x", "a@x"));
    assertFalse(PurchasingRules.bankChangeVerified("a@x", null));
    assertTrue(PurchasingRules.bankChangeVerified("a@x", "b@x"));
  }
}
