package eg.gov.health.platform.toxicology;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
class ToxicologyRulesTest {
  @Test void onlySpecialistsConsult() {
    assertTrue(ToxicologyRules.mayConsult("toxicologist"));
    assertTrue(ToxicologyRules.mayConsult("poison_center_specialist"));
    assertFalse(ToxicologyRules.mayConsult("receptionist"));
    assertFalse(ToxicologyRules.mayConsult(null));
  }
  @Test void referenceMustBeCurrent() {
    var today = LocalDate.of(2026, 11, 22);
    assertTrue(ToxicologyRules.referenceCurrent(true, LocalDate.of(2026, 1, 1), today));
    assertFalse(ToxicologyRules.referenceCurrent(true, LocalDate.of(2023, 1, 1), today)); // stale
    assertFalse(ToxicologyRules.referenceCurrent(false, LocalDate.of(2026, 1, 1), today)); // inactive
  }
  @Test void uncertaintyIsDocumentedNotAssumed() {
    assertTrue(ToxicologyRules.unknownSubstanceDocumented(false, null));
    assertFalse(ToxicologyRules.unknownSubstanceDocumented(true, null));
    assertFalse(ToxicologyRules.unknownSubstanceDocumented(true, " "));
    assertTrue(ToxicologyRules.unknownSubstanceDocumented(true, "زجاجة بيضاء بلا ملصق من مخزن مزرعة"));
  }
  @Test void noRecommendationWhenCenterUnavailable() {
    assertFalse(ToxicologyRules.recommendationAllowed(true, "give antidote"));
    assertTrue(ToxicologyRules.recommendationAllowed(true, null));
    assertTrue(ToxicologyRules.recommendationAllowed(false, "give antidote"));
  }
}
