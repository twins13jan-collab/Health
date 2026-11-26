package eg.gov.health.platform.admissionsdeep;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class AdmissionRulesTest {
  @Test void acceptanceCompletenessAndAma() {
    assertFalse(AdmissionRules.admittable("", false, false));
    assertFalse(AdmissionRules.admittable("قصور قلبي", true, false)); // transfer without acceptance
    assertTrue(AdmissionRules.admittable("قصور قلبي", true, true));
    assertTrue(AdmissionRules.admittable("قصور قلبي", false, false));
    var gaps = AdmissionRules.summaryGaps("ذبحة", "قسطرة", "راحة", "", "أسبرين", "متابعة أسبوع");
    assertTrue(gaps.contains("warnings"));
    assertTrue(AdmissionRules.summaryGaps("a","b","c","d","e","f").isEmpty());
    assertFalse(AdmissionRules.amaDocumented(true, false));
    assertTrue(AdmissionRules.amaDocumented(true, true));
  }
}
