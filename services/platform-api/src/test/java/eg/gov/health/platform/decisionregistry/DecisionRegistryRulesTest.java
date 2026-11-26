package eg.gov.health.platform.decisionregistry;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class DecisionRegistryRulesTest {
  @Test void aDecisionNeedsMakerContextAlternativesAndReasons() {
    assertTrue(DecisionRegistryRules.registerable("مدير", "لائحة", "سياق", "بدائل", "أسباب", false, false));
    assertFalse(DecisionRegistryRules.registerable("مدير", "لائحة", "سياق", "", "أسباب", false, false)); // no alternatives
    assertFalse(DecisionRegistryRules.registerable("", "لائحة", "سياق", "بدائل", "أسباب", false, false));
    assertFalse(DecisionRegistryRules.registerable("لجنة", "لائحة", "سياق", "بدائل", "أسباب", true, false)); // committee, COI unclear
    assertTrue(DecisionRegistryRules.registerable("لجنة", "لائحة", "سياق", "بدائل", "أسباب", true, true));
    assertFalse(DecisionRegistryRules.amendmentValid(" "));
    assertTrue(DecisionRegistryRules.amendmentValid("أدلة جديدة"));
  }
}
