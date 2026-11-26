package eg.gov.health.platform.governance;
import java.time.LocalDate;
/** Medical-record retention (documents module): records are retained for
 *  the national minimum period by category; disposal before the retention
 *  date is prohibited, and disposal after it requires a documented
 *  authorisation — the system enforces the date, humans authorise the act. */
public final class RetentionRules {
  private RetentionRules() {}
  public static int retentionYears(String category) {
    return switch (category == null ? "" : category) {
      case "death_certificate", "birth_record" -> 100; // permanent-class records
      case "operative_report", "discharge_summary", "consent_form" -> 25;
      case "lab_report", "radiology_report", "prescription_copy" -> 15;
      default -> 10;
    };
  }
  public static LocalDate disposalEligibleFrom(LocalDate documentDate, String category) {
    return documentDate.plusYears(retentionYears(category));
  }
  public static boolean disposalAllowed(LocalDate documentDate, String category, LocalDate onDate, boolean authorised) {
    return authorised && !onDate.isBefore(disposalEligibleFrom(documentDate, category));
  }
}
