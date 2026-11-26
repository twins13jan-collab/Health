package eg.gov.health.platform.telemedicine;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class TeleRulesTest {
  @Test void verifiedConsentAndHonestLimits() {
    assertFalse(TeleRules.startable(false, true));
    assertFalse(TeleRules.startable(true, false));
    assertTrue(TeleRules.startable(true, true));
    assertFalse(TeleRules.closable(false, "لا حاجة لحضور"));
    assertFalse(TeleRules.closable(true, " "));
    assertTrue(TeleRules.closable(true, "حضور خلال أسبوع إن لم تتحسن"));
  }
}
