package eg.gov.health.platform.infectioncontrol;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
class InfectionRulesTest {
  @Test void airborneNeedsNegativePressure() {
    assertTrue(InfectionRules.locationValid("airborne", "negative_pressure"));
    assertFalse(InfectionRules.locationValid("airborne", "single_room"));
    assertTrue(InfectionRules.locationValid("contact", "cohort_area"));
    assertFalse(InfectionRules.locationValid("contact", null));
  }
  @Test void onlyIpcDiscontinuesIsolation() {
    assertTrue(InfectionRules.mayDiscontinueIsolation("ipc_officer"));
    assertTrue(InfectionRules.mayDiscontinueIsolation("infection_control"));
    assertFalse(InfectionRules.mayDiscontinueIsolation("nurse"));
  }
  @Test void notificationBreachIsConfirmedNotifiableUnnotified() {
    assertTrue(InfectionRules.notificationBreach(true, "confirmed", false));
    assertFalse(InfectionRules.notificationBreach(true, "confirmed", true));
    assertFalse(InfectionRules.notificationBreach(true, "suspected", false));
    assertFalse(InfectionRules.notificationBreach(false, "confirmed", false));
  }
  @Test void haiRateComputedPer1000DeviceDays() {
    assertEquals(new BigDecimal("4.00"), InfectionRules.haiRatePer1000(2, 500));
    assertEquals(new BigDecimal("12.50"), InfectionRules.haiRatePer1000(5, 400));
    assertEquals(BigDecimal.ZERO, InfectionRules.haiRatePer1000(3, 0));
  }
  @Test void stewardshipFlagsPlainContinuation() {
    assertTrue(InfectionRules.stewardshipDecisionRequired("حساسية للأمبيسيلين", true, "continue"));
    assertFalse(InfectionRules.stewardshipDecisionRequired("حساسية للأمبيسيلين", true, "de_escalate"));
    assertFalse(InfectionRules.stewardshipDecisionRequired(null, true, "continue"));
  }
}
