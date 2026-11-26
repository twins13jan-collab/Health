package eg.gov.health.platform.connecteddevices;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
class DeviceRulesTest {
  @Test void calibrationSignalAndPlausibility() {
    assertTrue(DeviceRules.calibrationValid(LocalDate.parse("2026-12-31"), LocalDate.parse("2026-11-22")));
    assertFalse(DeviceRules.calibrationValid(LocalDate.parse("2026-06-30"), LocalDate.parse("2026-11-22")));
    assertFalse(DeviceRules.plausible("spo2", 140.0));
    assertTrue(DeviceRules.plausible("spo2", 97.0));
    assertFalse(DeviceRules.plausible("heart_rate", 480.0));
    assertFalse(DeviceRules.reliable(false, "good", true));
    assertFalse(DeviceRules.reliable(true, "poor", true));
    assertTrue(DeviceRules.reliable(true, "good", true));
  }
}
