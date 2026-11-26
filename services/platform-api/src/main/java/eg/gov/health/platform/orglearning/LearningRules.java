package eg.gov.health.platform.orglearning;
import java.time.*;
/** Organizational-learning rules (module 80). A knowledge
 *  document is current only inside its effective window and
 *  before its review date — an expired-review procedure still
 *  on the shelf is a hazard wearing a uniform. Reading is not
 *  understanding: acknowledgment counts toward mandatory
 *  training only with a comprehension assessment. And a lesson
 *  learned closes only when it has been APPLIED into a living
 *  document — a lesson filed is a lesson repeated. */
public final class LearningRules {
  private LearningRules() {}
  public static boolean current(LocalDate effectiveFrom, LocalDate reviewDue, LocalDate day) {
    if (day == null) return false;
    if (effectiveFrom != null && day.isBefore(effectiveFrom)) return false;
    return reviewDue == null || !day.isAfter(reviewDue);
  }
  public static boolean understood(boolean acknowledged, boolean assessed) { return acknowledged && assessed; }
  public static boolean lessonClosable(boolean applied, String appliedIn) {
    return applied && appliedIn != null && !appliedIn.isBlank();
  }
}
