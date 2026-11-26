package eg.gov.health.platform.emergencytriage;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
class TriageRulesTest {
  @Test void doorToDoctorScalesWithAcuity() {
    var t0 = OffsetDateTime.parse("2026-11-22T10:00:00+02:00");
    assertTrue(TriageRules.doorToDoctorBreached(2, t0, t0.plusMinutes(25)));  // ESI2 target 10
    assertFalse(TriageRules.doorToDoctorBreached(2, t0, t0.plusMinutes(8)));
    assertFalse(TriageRules.doorToDoctorBreached(4, t0, t0.plusMinutes(45))); // ESI4 target 60
    assertTrue(TriageRules.doorToDoctorBreached(1, t0, t0.plusMinutes(3)));   // ESI1 immediate
    assertFalse(TriageRules.departureDocumented(true, false));
    assertTrue(TriageRules.departureDocumented(true, true));
  }
}
