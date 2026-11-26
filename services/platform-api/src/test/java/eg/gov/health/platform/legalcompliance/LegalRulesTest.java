package eg.gov.health.platform.legalcompliance;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
class LegalRulesTest {
  @Test void requirementsBindOnlyInsideTheirWindow() {
    var today = LocalDate.parse("2026-11-22");
    assertTrue(LegalRules.requirementApplicable(LocalDate.parse("2025-01-01"), null, today));
    assertFalse(LegalRules.requirementApplicable(LocalDate.parse("2027-01-01"), null, today)); // not yet in force
    assertFalse(LegalRules.requirementApplicable(LocalDate.parse("2020-01-01"), LocalDate.parse("2025-12-31"), today)); // repealed
  }
  @Test void exceptionsAreAuthorityBackedAndTimeBoxed() {
    var today = LocalDate.parse("2026-11-22");
    assertTrue(LegalRules.exceptionValid("قرار وزاري 12", LocalDate.parse("2027-06-01"), today));
    assertFalse(LegalRules.exceptionValid("قرار وزاري 12", null, today));       // perpetual
    assertFalse(LegalRules.exceptionValid(" ", LocalDate.parse("2027-06-01"), today)); // no authority
    assertFalse(LegalRules.exceptionValid("قرار", LocalDate.parse("2026-01-01"), today)); // already expired
  }
  @Test void nonComplianceClosesOnlyThroughCorrection() {
    assertFalse(LegalRules.reviewClosable(false, false));
    assertTrue(LegalRules.reviewClosable(false, true));
    assertTrue(LegalRules.reviewClosable(true, false));
  }
}
