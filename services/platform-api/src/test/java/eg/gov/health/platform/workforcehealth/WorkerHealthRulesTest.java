package eg.gov.health.platform.workforcehealth;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
class WorkerHealthRulesTest {
  @Test void fitnessExpiresWithExamAndVaccinations() {
    var today = LocalDate.parse("2026-11-22");
    assertTrue(WorkerHealthRules.fitForExposureDuties(LocalDate.parse("2026-02-01"), true, today));
    assertFalse(WorkerHealthRules.fitForExposureDuties(LocalDate.parse("2025-06-01"), true, today)); // exam stale
    assertFalse(WorkerHealthRules.fitForExposureDuties(LocalDate.parse("2026-02-01"), false, today)); // vaccinations out of date
    assertFalse(WorkerHealthRules.fitForExposureDuties(null, true, today));
  }
  @Test void exposureFilesCloseOnlyAfterInvestigationAndFollowUp() {
    assertFalse(WorkerHealthRules.incidentClosable(false, true));
    assertFalse(WorkerHealthRules.incidentClosable(true, false));
    assertTrue(WorkerHealthRules.incidentClosable(true, true));
    assertFalse(WorkerHealthRules.ppeCompliant(false));
  }
}
