package eg.gov.health.platform.notify;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class NotificationRulesTest {
  @Test void honestDeliveryLifecycle() {
    assertTrue(NotificationRules.knownChannel("sms"));
    assertFalse(NotificationRules.knownChannel("pigeon"));
    assertTrue(NotificationRules.dispatchable("queued", 0));
    assertFalse(NotificationRules.dispatchable("queued", 3));
    assertFalse(NotificationRules.dispatchable("sent", 0));
    assertEquals("queued", NotificationRules.statusAfterFailure(1));
    assertEquals("dead", NotificationRules.statusAfterFailure(3));
  }
}
