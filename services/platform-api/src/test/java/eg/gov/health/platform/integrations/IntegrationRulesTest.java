package eg.gov.health.platform.integrations;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class IntegrationRulesTest {
  @Test void http200IsNotSuccess() {
    assertFalse(IntegrationRules.fullyAcknowledged("ACK", false)); // transport only
    assertFalse(IntegrationRules.fullyAcknowledged("NACK", true));
    assertTrue(IntegrationRules.fullyAcknowledged("ACK", true));
  }
  @Test void gradedRetriesExhaustToDeadLetter() {
    assertFalse(IntegrationRules.deadLetter(1));
    assertFalse(IntegrationRules.deadLetter(2));
    assertTrue(IntegrationRules.deadLetter(3));
  }
  @Test void activationNeedsCompatibilityTest() {
    assertFalse(IntegrationRules.activatable(false));
    assertTrue(IntegrationRules.activatable(true));
  }
}
