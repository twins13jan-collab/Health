package eg.gov.health.platform.sensory;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
class SensoryRulesTest {
  @Test void measurementNeedsSideUnitDeviceMethod() {
    assertTrue(SensoryRules.measurementComplete("left", "dB", "AUD-1", "pure_tone"));
    assertFalse(SensoryRules.measurementComplete("left", null, "AUD-1", "pure_tone")); // no unit
    assertFalse(SensoryRules.measurementComplete("up", "dB", "AUD-1", "pure_tone"));   // bad side
    assertFalse(SensoryRules.measurementComplete("left", "dB", " ", "pure_tone"));      // no device
  }
  @Test void calibrationCurrency() {
    var today = LocalDate.of(2026, 11, 22);
    assertTrue(SensoryRules.calibrationCurrent(true, LocalDate.of(2026, 5, 1), today));
    assertFalse(SensoryRules.calibrationCurrent(true, LocalDate.of(2024, 1, 1), today)); // stale > 12 months
    assertFalse(SensoryRules.calibrationCurrent(false, LocalDate.of(2026, 5, 1), today)); // failed
  }
  @Test void plausibilityFlags() {
    assertTrue(SensoryRules.iopSuspect(85.0));
    assertFalse(SensoryRules.iopSuspect(16.0));
    assertTrue(SensoryRules.acuityPlausible(1.0));
    assertFalse(SensoryRules.acuityPlausible(3.5));
  }
  @Test void deliveryNeedsFitting() {
    assertTrue(SensoryRules.deliverable("fitted"));
    assertFalse(SensoryRules.deliverable("pending"));
    assertFalse(SensoryRules.deliverable("refit_required"));
  }
}
