package eg.gov.health.platform.capabilityaccess;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
class CapabilityRulesTest {
  @Test void allPillarsAndFreshVerification() {
    var now = OffsetDateTime.parse("2026-11-22T00:00:00+02:00");
    assertTrue(CapabilityRules.available(true, true, true, true, now.minusDays(5), now));
    assertFalse(CapabilityRules.available(true, false, true, true, now.minusDays(5), now));
    assertFalse(CapabilityRules.available(true, true, true, true, now.minusDays(45), now)); // stale
    assertTrue(CapabilityRules.missingPillars(true, true, false, true).contains("licence_invalid"));
  }
}
