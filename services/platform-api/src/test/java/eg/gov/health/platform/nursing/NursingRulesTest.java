package eg.gov.health.platform.nursing;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
class NursingRulesTest {
  @Test void assignmentFollowsLicenceAndWorkload() {
    assertTrue(NursingRules.assignmentPermitted("general, blood", "blood", 3));
    assertFalse(NursingRules.assignmentPermitted("general", "blood", 3)); // blood needs the blood scope
    assertTrue(NursingRules.assignmentPermitted("general", "medication", 3));
    assertFalse(NursingRules.assignmentPermitted("general, blood", "blood", 12)); // workload cap
  }
  @Test void overdueTasksSurfaceAndHandoverIsStructured() {
    var past = OffsetDateTime.now().minusHours(2);
    assertTrue(NursingRules.isOverdue(past, "assigned", OffsetDateTime.now()));
    assertFalse(NursingRules.isOverdue(past, "done", OffsetDateTime.now()));
    assertTrue(NursingRules.handoverComplete("حالتان حرجتان بالعناية", 4));
    assertFalse(NursingRules.handoverComplete(null, 4));
    assertFalse(NursingRules.handoverComplete("حالات", null));
  }
}
