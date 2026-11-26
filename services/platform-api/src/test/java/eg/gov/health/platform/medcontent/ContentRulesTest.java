package eg.gov.health.platform.medcontent;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
class ContentRulesTest {
  @Test void publicationNeedsIndependentReviewAndDocumentation() {
    var d = LocalDate.parse("2026-11-01");
    assertTrue(ContentRules.publishable("approved", "author@x", "reviewer@x", "وزارة الصحة", "مراجع إكلينيكية", d));
    assertFalse(ContentRules.publishable("approved", "same@x", "same@x", "مصدر", "مراجع", d)); // self-review
    assertFalse(ContentRules.publishable("approved", "a@x", "r@x", null, "مراجع", d));          // no source
    assertFalse(ContentRules.publishable("draft", "a@x", "r@x", "مصدر", "مراجع", d));           // not approved
  }
  @Test void reviewsExpireAfterTwelveMonths() {
    var reviewed = LocalDate.parse("2025-06-01");
    assertEquals(LocalDate.parse("2026-06-01"), ContentRules.reviewDue(reviewed));
    assertTrue(ContentRules.stale(LocalDate.parse("2026-06-01"), LocalDate.parse("2026-06-02")));
    assertFalse(ContentRules.stale(LocalDate.parse("2026-06-01"), LocalDate.parse("2026-05-30")));
  }
  @Test void comprehensionThresholdIsSeventy() {
    assertTrue(ContentRules.comprehensionAdequate(84.0));
    assertFalse(ContentRules.comprehensionAdequate(55.0));
  }
}
