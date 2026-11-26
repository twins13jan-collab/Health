package eg.gov.health.platform.criticalcare;
import org.junit.jupiter.api.Test;
import java.time.OffsetDateTime;
import static org.junit.jupiter.api.Assertions.*;
class IcuRulesTest {
  @Test void freshnessBands() {
    var now = OffsetDateTime.now();
    assertEquals(IcuRules.Freshness.LIVE, IcuRules.freshness(now.minusSeconds(30), now));
    assertEquals(IcuRules.Freshness.STALE, IcuRules.freshness(now.minusSeconds(300), now));
    assertEquals(IcuRules.Freshness.SIGNAL_LOST, IcuRules.freshness(now.minusSeconds(3600), now));
    assertTrue(IcuRules.mayPresentAsLive(IcuRules.Freshness.LIVE));
    assertFalse(IcuRules.mayPresentAsLive(IcuRules.Freshness.STALE));
  }
  @Test void implausibleValuesFlaggedSuspect() {
    assertEquals("good", IcuRules.qualityFor("spo2", 98));
    assertEquals("suspect", IcuRules.qualityFor("spo2", 140));
    assertEquals("suspect", IcuRules.qualityFor("heart_rate", 400));
    assertEquals("good", IcuRules.qualityFor("gcs", 15));
  }
  @Test void weightNormalisationAndDoseGate() {
    assertEquals(70.0, IcuRules.normalizedWeightKg(70, "kg"));
    assertEquals(68.0, IcuRules.normalizedWeightKg(150, "lb"), 0.11);
    assertFalse(IcuRules.doseCalculationAllowed(false));
    assertTrue(IcuRules.doseCalculationAllowed(true));
    assertEquals(35.0, IcuRules.totalRate(0.5, 70.0));
  }
  @Test void alarmSilenceCapped() {
    assertTrue(IcuRules.silenceAllowed(120));
    assertFalse(IcuRules.silenceAllowed(121));
    assertFalse(IcuRules.silenceAllowed(Integer.MAX_VALUE));
  }
}
