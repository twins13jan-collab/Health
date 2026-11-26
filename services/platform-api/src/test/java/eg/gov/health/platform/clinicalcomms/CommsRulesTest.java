package eg.gov.health.platform.clinicalcomms;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
class CommsRulesTest {
  @Test void loopClosesOnlyOnAcknowledgeAndReadBack() {
    assertTrue(CommsRules.loopClosed(true, true));
    assertFalse(CommsRules.loopClosed(true, false)); // acknowledged is not understood
    assertFalse(CommsRules.loopClosed(false, true));
  }
  @Test void silentAlertsEscalate() {
    var sent = OffsetDateTime.now().minusMinutes(20);
    assertTrue(CommsRules.escalationDue(sent, false, OffsetDateTime.now()));
    assertFalse(CommsRules.escalationDue(sent, true, OffsetDateTime.now()));
    assertFalse(CommsRules.escalationDue(OffsetDateTime.now().minusMinutes(5), false, OffsetDateTime.now()));
  }
}
