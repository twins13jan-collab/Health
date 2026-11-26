package eg.gov.health.platform.governance;
import org.junit.jupiter.api.Test; import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class ResearchExtractRulesTest {
  @Test void directIdentifiersAlwaysViolations() {
    assertEquals(List.of("national_id"), ResearchExtractRules.violations(List.of("age_band","national_id")));
  }
  @Test void extractNeedsApprovalAndCleanFields() {
    assertFalse(ResearchExtractRules.extractAllowed("submitted", List.of("age_band")));
    assertTrue(ResearchExtractRules.extractAllowed("approved", List.of("age_band","governorate")));
    assertFalse(ResearchExtractRules.extractAllowed("approved", List.of("phone")));
  }
}
