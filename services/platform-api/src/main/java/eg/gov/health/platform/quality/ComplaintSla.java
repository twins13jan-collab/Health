package eg.gov.health.platform.quality;
import java.time.*;
/** Complaint handling SLA (citizen grievances): resolution due dates by
 *  severity, computed in business terms from the intake timestamp. A breach
 *  escalates automatically to the next management level. */
public final class ComplaintSla {
  private ComplaintSla() {}
  public static int resolutionDays(String severity) {
    return switch (severity == null ? "" : severity) {
      case "critical" -> 3; case "high" -> 7; case "medium" -> 14; default -> 30;
    };
  }
  public static OffsetDateTime dueAt(OffsetDateTime intakeAt, String severity) { return intakeAt.plusDays(resolutionDays(severity)); }
  public static boolean isBreached(OffsetDateTime intakeAt, String severity, String status, OffsetDateTime now) {
    return !"resolved".equals(status) && !"closed".equals(status) && now.isAfter(dueAt(intakeAt, severity));
  }
}
