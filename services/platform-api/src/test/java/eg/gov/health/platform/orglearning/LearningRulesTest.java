package eg.gov.health.platform.orglearning;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
class LearningRulesTest {
  @Test void currencyUnderstandingAndAppliedLessons() {
    assertTrue(LearningRules.current(LocalDate.parse("2026-01-01"), LocalDate.parse("2026-12-31"), LocalDate.parse("2026-11-22")));
    assertFalse(LearningRules.current(LocalDate.parse("2026-01-01"), LocalDate.parse("2026-06-30"), LocalDate.parse("2026-11-22"))); // review overdue
    assertFalse(LearningRules.understood(true, false)); // read but never assessed
    assertTrue(LearningRules.understood(true, true));
    assertFalse(LearningRules.lessonClosable(false, "دليل الإخلاء v3"));
    assertFalse(LearningRules.lessonClosable(true, " "));
    assertTrue(LearningRules.lessonClosable(true, "دليل الإخلاء v3"));
  }
}
