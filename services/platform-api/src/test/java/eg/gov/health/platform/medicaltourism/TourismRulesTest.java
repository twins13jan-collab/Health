package eg.gov.health.platform.medicaltourism;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class TourismRulesTest {
  @Test void noSurprisesAndLawfulBorders() {
    assertTrue(TourismRules.schedulable(true, true, true));
    assertFalse(TourismRules.schedulable(true, true, false)); // estimate not approved
    assertFalse(TourismRules.schedulable(false, true, true)); // file untranslated
    assertFalse(TourismRules.transferLawful("اتفاقية ثنائية", false)); // basis without consent
    assertFalse(TourismRules.transferLawful(null, true));              // consent without basis
    assertTrue(TourismRules.transferLawful("اتفاقية ثنائية + قانون حماية البيانات", true));
    assertFalse(TourismRules.followupPermitted(false));
  }
}
