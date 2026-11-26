package eg.gov.health.platform.medicalassets;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
class BiomedBoardRulesTest {
  private static final LocalDate TODAY = LocalDate.of(2026, 11, 23);
  @Test void calibrationStates() {
    assertEquals("overdue", BiomedBoardRules.calibrationState(TODAY.minusDays(1), TODAY));
    assertEquals("due_soon", BiomedBoardRules.calibrationState(TODAY.plusDays(30), TODAY));
    assertEquals("current", BiomedBoardRules.calibrationState(TODAY.plusDays(31), TODAY));
    assertEquals("unscheduled", BiomedBoardRules.calibrationState(null, TODAY));
    assertEquals("due_soon", BiomedBoardRules.calibrationState(TODAY, TODAY));
  }
  @Test void maintenanceStatesUseTheShorterWindow() {
    assertEquals("overdue", BiomedBoardRules.maintenanceState(TODAY.minusDays(2), TODAY));
    assertEquals("due_soon", BiomedBoardRules.maintenanceState(TODAY.plusDays(14), TODAY));
    assertEquals("current", BiomedBoardRules.maintenanceState(TODAY.plusDays(15), TODAY));
  }
  @Test void patientAffectingIncidentTopsEverything() {
    assertEquals(0, BiomedBoardRules.assetRank("in_service", "current", "current", true));
    assertTrue(BiomedBoardRules.assetRank("in_service", "current", "current", true)
        < BiomedBoardRules.assetRank("out_of_service", "overdue", "overdue", false));
  }
  @Test void outOfCalibrationOutranksRoutineStates() {
    assertEquals(2, BiomedBoardRules.assetRank("in_service", "overdue", "current", false));
    assertTrue(BiomedBoardRules.assetRank("in_service", "overdue", "current", false)
        < BiomedBoardRules.assetRank("under_maintenance", "current", "current", false));
    assertTrue(BiomedBoardRules.assetRank("in_service", "current", "overdue", false)
        < BiomedBoardRules.assetRank("in_service", "current", "current", false));
  }
  @Test void healthyAssetLeavesTheQueue() {
    assertEquals(9, BiomedBoardRules.assetRank("in_service", "current", "current", false));
    assertEquals(9, BiomedBoardRules.assetRank("in_service", "unscheduled", "unscheduled", false));
  }
}
