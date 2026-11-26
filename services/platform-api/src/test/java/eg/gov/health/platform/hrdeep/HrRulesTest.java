package eg.gov.health.platform.hrdeep;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
class HrRulesTest {
  @Test void licenceTrainingAndGrievance() {
    assertFalse(HrRules.assignable(true, false));
    assertTrue(HrRules.assignable(true, true));
    assertTrue(HrRules.assignable(false, false));
    var today = LocalDate.parse("2026-11-22");
    assertFalse(HrRules.trainingCompliant(true, false, LocalDate.parse("2026-10-01"), today)); // overdue
    assertTrue(HrRules.trainingCompliant(true, false, LocalDate.parse("2026-12-31"), today));  // still in window
    assertTrue(HrRules.trainingCompliant(true, true, LocalDate.parse("2026-10-01"), today));
    assertFalse(HrRules.grievanceClosable(true, " "));
    assertTrue(HrRules.grievanceClosable(true, "اعتذار رسمي وتعويض وردية"));
  }
}
