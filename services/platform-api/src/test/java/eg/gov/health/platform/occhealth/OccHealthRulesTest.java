package eg.gov.health.platform.occhealth;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
class OccHealthRulesTest {
  @Test void overdueSurveillanceRemovesClearance() {
    assertTrue(OccHealthRules.examOverdue(LocalDate.parse("2026-01-01"), LocalDate.parse("2026-11-22")));
    assertFalse(OccHealthRules.clearedForDuty("fit", true, false)); // fit but overdue = not cleared
    assertTrue(OccHealthRules.clearedForDuty("fit", false, false));
    assertTrue(OccHealthRules.clearedForDuty("fit_with_restrictions", false, false));
    assertFalse(OccHealthRules.clearedForDuty("unfit", false, false));
  }
  @Test void openExposureBlocksReturnToDuty() {
    assertFalse(OccHealthRules.clearedForDuty("fit", false, true));
  }
  @Test void employerDisclosureNeverCarriesTheDiagnosis() {
    var d = OccHealthRules.employerDisclosure("fit_with_restrictions", "لا عمل ليلي");
    assertEquals("fit_with_restrictions", d.get("fitness_status"));
    assertFalse(d.toString().toLowerCase().contains("diagnosis_note"));
    assertTrue(d.keySet().stream().noneMatch(k -> k.contains("diagnos")));
  }
}
