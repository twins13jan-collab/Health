package eg.gov.health.platform.clinicaltrials;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class TrialRulesTest {
  @Test void recruitmentNeedsEthicsRegulatoryAndCoi() {
    assertTrue(TrialRules.recruitmentPermitted(true, true, true));
    assertFalse(TrialRules.recruitmentPermitted(false, true, true));
    assertFalse(TrialRules.recruitmentPermitted(true, false, true));
    assertFalse(TrialRules.recruitmentPermitted(true, true, false)); // undeclared conflicts bar recruitment
  }
  @Test void researchActivityNeedsActiveConsent() {
    assertTrue(TrialRules.researchActivityPermitted("consented"));
    assertFalse(TrialRules.researchActivityPermitted("not_consented"));
    assertFalse(TrialRules.researchActivityPermitted("withdrawn")); // withdrawal is immediate
  }
  @Test void unreportedSaeIsABreach() {
    assertTrue(TrialRules.saeBreach("serious", false));
    assertFalse(TrialRules.saeBreach("serious", true));
    assertFalse(TrialRules.saeBreach("moderate", false));
  }
}
