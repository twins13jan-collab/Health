package eg.gov.health.platform.porthealth;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
class PortHealthRulesTest {
  @Test void highRiskSymptomaticTravelersAreNeverCleared() {
    assertFalse(PortHealthRules.screeningPermitted("high", "cleared"));
    assertTrue(PortHealthRules.screeningPermitted("high", "isolated"));
    assertTrue(PortHealthRules.screeningPermitted("high", "referred"));
    assertTrue(PortHealthRules.screeningPermitted("low", "cleared"));
  }
  @Test void earlyReleaseNeedsAuthorizerAndReason() {
    var now = OffsetDateTime.parse("2026-11-22T10:00:00+02:00");
    var ends = now.plusDays(5);
    assertFalse(PortHealthRules.earlyReleasePermitted(now, ends, "officer@x", null));
    assertFalse(PortHealthRules.earlyReleasePermitted(now, ends, null, "سبب"));
    assertTrue(PortHealthRules.earlyReleasePermitted(now, ends, "officer@x", "نتيجتان سلبيتان"));
    assertTrue(PortHealthRules.quarantineComplete(now.plusDays(6), ends));
  }
}
