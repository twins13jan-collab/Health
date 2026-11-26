package eg.gov.health.platform.medcontent;
import java.time.*;
/** Health-education content rules (module 120). Nothing reaches a
 *  patient unreviewed: publication requires clinical approval by a
 *  reviewer who is NOT the author, with the source and references
 *  documented and a review date set. Reviews expire — content past
 *  its review-due date is stale and must be re-reviewed or
 *  withdrawn, never quietly kept live. Campaigns measure
 *  comprehension; material the audience does not understand (score
 *  under 70) is flagged for revision, not celebrated for reach. */
public final class ContentRules {
  private ContentRules() {}
  public static boolean publishable(String status, String author, String reviewer, String sourceRef, String referencesText, LocalDate reviewedAt) {
    return "approved".equals(status)
      && reviewer != null && author != null && !reviewer.equals(author)
      && sourceRef != null && !sourceRef.isBlank()
      && referencesText != null && !referencesText.isBlank()
      && reviewedAt != null;
  }
  public static LocalDate reviewDue(LocalDate reviewedAt) {
    return reviewedAt == null ? null : reviewedAt.plusMonths(12);
  }
  public static boolean stale(LocalDate reviewDueAt, LocalDate today) {
    return reviewDueAt != null && today != null && today.isAfter(reviewDueAt);
  }
  public static boolean comprehensionAdequate(Double score) {
    return score != null && score >= 70.0;
  }
}
