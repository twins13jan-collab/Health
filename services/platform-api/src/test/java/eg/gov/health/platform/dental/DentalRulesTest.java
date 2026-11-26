package eg.gov.health.platform.dental;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
class DentalRulesTest {
  @Test void fdiNotation() {
    assertTrue(DentalRules.validFdi(11)); assertTrue(DentalRules.validFdi(36));
    assertTrue(DentalRules.validFdi(48)); assertTrue(DentalRules.validFdi(55)); // deciduous
    assertFalse(DentalRules.validFdi(19)); assertFalse(DentalRules.validFdi(99));
    assertFalse(DentalRules.validFdi(10)); assertFalse(DentalRules.validFdi(40));
  }
  @Test void sterilizationGate() {
    var now = OffsetDateTime.now();
    assertTrue(DentalRules.sterilizationValid("passed", now.plusHours(24), now));
    assertFalse(DentalRules.sterilizationValid("failed", now.plusHours(24), now));
    assertFalse(DentalRules.sterilizationValid("passed", now.minusHours(1), now));
  }
  @Test void siteAndAnesthesiaGates() {
    assertTrue(DentalRules.siteReady(true, 36));
    assertFalse(DentalRules.siteReady(false, 36));
    assertFalse(DentalRules.siteReady(true, 99));
    assertTrue(DentalRules.anesthesiaCleared(false, false));
    assertFalse(DentalRules.anesthesiaCleared(true, false));
    assertTrue(DentalRules.anesthesiaCleared(true, true));
  }
}
