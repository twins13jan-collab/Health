package eg.gov.health.platform.hta;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class HtaRulesTest {
  @Test void appraisalNeedsCleanCoiAndStakeholders() {
    assertTrue(HtaRules.appraisalPermitted(5, 0, true));
    assertFalse(HtaRules.appraisalPermitted(5, 1, true));  // one member never declared COI
    assertFalse(HtaRules.appraisalPermitted(5, 0, false)); // stakeholders not consulted
    assertFalse(HtaRules.appraisalPermitted(0, 0, true));  // no panel at all
  }
  @Test void recommendationStrengthIsCappedByEvidenceQuality() {
    assertTrue(HtaRules.recommendationPermitted("adopt", "moderate", "effective", "acceptable"));
    assertTrue(HtaRules.recommendationPermitted("adopt", "high", "effective", "acceptable"));
    assertFalse(HtaRules.recommendationPermitted("adopt", "low", "effective", "acceptable"));
    assertFalse(HtaRules.recommendationPermitted("adopt_with_restrictions", "very_low", "effective", "acceptable"));
    assertFalse(HtaRules.recommendationPermitted("adopt", "high", "uncertain", "acceptable"));
    assertTrue(HtaRules.recommendationPermitted("research_only", "low", "uncertain", "acceptable"));
    assertTrue(HtaRules.recommendationPermitted("reject", "very_low", "ineffective", "unacceptable"));
  }
}
