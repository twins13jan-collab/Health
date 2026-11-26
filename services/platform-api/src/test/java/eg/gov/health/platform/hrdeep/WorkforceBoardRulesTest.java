package eg.gov.health.platform.hrdeep;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
class WorkforceBoardRulesTest {
  static final LocalDate TODAY = LocalDate.of(2026, 11, 23);
  @Test void lapsedClinicalLicenceBlocksAssignmentAndTopsQueue() {
    assertEquals("licence_blocked", WorkforceBoardRules.employeeState(true, false, 0));
    assertEquals(0, WorkforceBoardRules.employeeRank("licence_blocked"));
    assertEquals("compliant", WorkforceBoardRules.employeeState(false, false, 0));
  }
  @Test void overdueMandatoryTrainingFlagsNonCompliance() {
    assertEquals("training_overdue", WorkforceBoardRules.employeeState(true, true, 2));
    assertTrue(WorkforceBoardRules.employeeRank("training_overdue") > WorkforceBoardRules.employeeRank("licence_blocked"));
    assertEquals("compliant", WorkforceBoardRules.employeeState(true, true, 0));
  }
  @Test void trainingStatesByDueDate() {
    assertEquals("overdue", WorkforceBoardRules.trainingState(true, false, TODAY.minusDays(1), TODAY));
    assertEquals("due_soon", WorkforceBoardRules.trainingState(true, false, TODAY.plusDays(30), TODAY));
    assertEquals("open", WorkforceBoardRules.trainingState(true, false, TODAY.plusDays(31), TODAY));
    assertEquals("completed", WorkforceBoardRules.trainingState(true, true, TODAY.minusDays(90), TODAY));
    assertEquals("open", WorkforceBoardRules.trainingState(false, false, TODAY.minusDays(90), TODAY));
  }
  @Test void unreviewedGrievanceOutranksReviewedOne() {
    assertEquals(0, WorkforceBoardRules.grievanceRank("open", false));
    assertEquals(1, WorkforceBoardRules.grievanceRank("open", true));
    assertEquals(9, WorkforceBoardRules.grievanceRank("closed", true));
  }
  @Test void completedTrainingLeavesTheQueue() {
    assertEquals(9, WorkforceBoardRules.trainingRank("completed"));
    assertTrue(WorkforceBoardRules.trainingRank("overdue") < WorkforceBoardRules.trainingRank("due_soon"));
  }
}
