package eg.gov.health.platform.multilingual;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class LanguageRulesTest {
  @Test void medicalReviewAndRealUserTesting() {
    assertFalse(LanguageRules.publishable(false));
    assertTrue(LanguageRules.publishable(true));
    assertFalse(LanguageRules.accessibilityClaimValid(true, false));
    assertFalse(LanguageRules.accessibilityClaimValid(false, true));
    assertTrue(LanguageRules.accessibilityClaimValid(true, true));
  }
}
