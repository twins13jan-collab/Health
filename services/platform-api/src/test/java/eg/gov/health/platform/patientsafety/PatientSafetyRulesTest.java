package eg.gov.health.platform.patientsafety;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class PatientSafetyRulesTest {
  @Test void closureNeedsNotificationAnalysisAndPrevention() {
    assertFalse(PatientSafetyRules.closable("harm", false, true, true));  // harmed patient never told
    assertTrue(PatientSafetyRules.closable("harm", true, true, true));
    assertFalse(PatientSafetyRules.closable("near_miss", false, true, false)); // prevention incomplete
    assertTrue(PatientSafetyRules.closable("near_miss", false, true, true));   // near miss: no patient to notify
  }
}
