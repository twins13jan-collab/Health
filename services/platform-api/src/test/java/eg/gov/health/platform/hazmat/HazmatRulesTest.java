package eg.gov.health.platform.hazmat;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class HazmatRulesTest {
  @Test void contaminatedCasualtiesAreDecontaminatedBeforeTreatment() {
    assertFalse(HazmatRules.treatmentEntryPermitted(true, false));
    assertTrue(HazmatRules.treatmentEntryPermitted(true, true));
    assertTrue(HazmatRules.treatmentEntryPermitted(false, false));
  }
  @Test void closureWaitsForZonesCoordinationAndFollowUps() {
    assertTrue(HazmatRules.specialistCoordinationRequired("chemical"));
    assertTrue(HazmatRules.specialistCoordinationRequired("radiological"));
    assertFalse(HazmatRules.specialistCoordinationRequired("environmental"));
    assertFalse(HazmatRules.incidentClosable(false, true, true, 0));  // no zones
    assertFalse(HazmatRules.incidentClosable(true, true, false, 0));  // coordination missing
    assertFalse(HazmatRules.incidentClosable(true, true, true, 2));   // follow-ups owed
    assertTrue(HazmatRules.incidentClosable(true, true, true, 0));
    assertTrue(HazmatRules.incidentClosable(true, false, false, 0));
  }
}
