package eg.gov.health.platform.accreditation;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
class AccreditationRulesTest {
  @Test void licencesAreDateBoundPermissions() {
    var today = LocalDate.parse("2026-11-22");
    assertTrue(AccreditationRules.licenceValid("active", LocalDate.parse("2027-05-01"), today));
    assertFalse(AccreditationRules.licenceValid("active", LocalDate.parse("2026-05-01"), today)); // expired
    assertFalse(AccreditationRules.licenceValid("suspended", LocalDate.parse("2027-05-01"), today));
  }
  @Test void findingsCloseOnlyThroughCorrection() {
    assertFalse(AccreditationRules.inspectionClosable("باب مخزن أدوية بلا قفل", false));
    assertTrue(AccreditationRules.inspectionClosable("باب مخزن أدوية بلا قفل", true));
    assertTrue(AccreditationRules.inspectionClosable("", false));
  }
}
