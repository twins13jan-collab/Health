package eg.gov.health.platform.organbank;
import org.junit.jupiter.api.Test;
import java.math.*; import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
class OrganRulesTest {
  @Test void allocationNeedsTheFullLegalChain() {
    assertTrue(OrganRules.allocationPermitted(true, true, true, false, true));
    assertFalse(OrganRules.allocationPermitted(false, true, true, false, true)); // no documented consent
    assertFalse(OrganRules.allocationPermitted(true, false, true, false, true)); // evaluation incomplete
    assertFalse(OrganRules.allocationPermitted(true, true, true, false, false)); // no committee
  }
  @Test void incentiveFlagIsAHardStop() {
    assertFalse(OrganRules.allocationPermitted(true, true, true, true, true));
    assertTrue(OrganRules.caseBlocked(true, true));
    assertTrue(OrganRules.caseBlocked(false, false)); // unscreened coercion also blocks
  }
  @Test void ischemiaLimitsArePerGraft() {
    var start = OffsetDateTime.parse("2026-11-22T08:00:00+02:00");
    assertFalse(OrganRules.ischemiaBreached("heart", start, start.plusHours(5)));
    assertTrue(OrganRules.ischemiaBreached("heart", start, start.plusHours(7)));  // heart limit 6h
    assertFalse(OrganRules.ischemiaBreached("kidney", start, start.plusHours(7))); // kidney limit 24h
    assertTrue(OrganRules.ischemiaBreached("kidney", start, start.plusHours(25)));
  }
  @Test void preservationTemperatureRange() {
    assertFalse(OrganRules.temperatureExcursion("kidney", new BigDecimal("4.0")));
    assertTrue(OrganRules.temperatureExcursion("kidney", new BigDecimal("11.5")));
    assertTrue(OrganRules.temperatureExcursion("kidney", new BigDecimal("-1")));
  }
}
