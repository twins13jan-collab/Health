package eg.gov.health.platform.nursing;
import java.time.*;
import java.util.List;

/** Pure rules of the medication administration record and nursing
 *  rounds. Frequencies arrive as free text from prescriptions, so
 *  the mapping to administration times is explicit and total:
 *  a known pattern yields its times, PRN yields none (given on
 *  demand, recorded manually), and anything unrecognised falls
 *  back to a single morning dose — never silently dropped. */
public final class MarRules {
  private MarRules() {}

  public static List<LocalTime> timesFor(String frequency) {
    if (frequency == null) return List.of(LocalTime.of(9, 0));
    String f = frequency.trim().toLowerCase();
    if (f.contains("prn") || f.contains("عند الحاجة") || f.contains("عند اللزوم")) return List.of();
    if (f.contains("q6h") || f.contains("كل 6")) return List.of(LocalTime.of(0, 0), LocalTime.of(6, 0), LocalTime.of(12, 0), LocalTime.of(18, 0));
    if (f.contains("q8h") || f.contains("كل 8")) return List.of(LocalTime.of(6, 0), LocalTime.of(14, 0), LocalTime.of(22, 0));
    if (f.contains("q12h") || f.contains("كل 12")) return List.of(LocalTime.of(9, 0), LocalTime.of(21, 0));
    if (f.contains("qid") || f.contains("4 مرات") || f.contains("اربع")) return List.of(LocalTime.of(6, 0), LocalTime.of(12, 0), LocalTime.of(18, 0), LocalTime.of(0, 0));
    if (f.contains("tid") || f.contains("3 مرات") || f.contains("ثلاث")) return List.of(LocalTime.of(6, 0), LocalTime.of(14, 0), LocalTime.of(22, 0));
    if (f.contains("bid") || f.contains("مرتين") || f.contains("2 مرة")) return List.of(LocalTime.of(9, 0), LocalTime.of(21, 0));
    // once daily and everything else: one morning dose
    return List.of(LocalTime.of(9, 0));
  }

  /** Recording a dose: "given" needs nothing more; every other
   *  outcome is a clinical fact that must carry its reason. */
  public static String administrationProblem(String status, String reason) {
    if (status == null) return "حالة الإعطاء مطلوبة";
    switch (status) {
      case "given": return null;
      case "refused": case "held": case "missed":
        return (reason == null || reason.isBlank()) ? "سبب عدم الإعطاء إلزامي وموثق" : null;
      default: return "حالة إعطاء غير معروفة: " + status;
    }
  }

  /** due = scheduled time reached; overdue = still unrecorded an
   *  hour after the scheduled time — the ward board escalates it. */
  public static String doseState(OffsetDateTime scheduledAt, String status, OffsetDateTime now) {
    if (!"scheduled".equals(status)) return status;
    if (now.isAfter(scheduledAt.plusMinutes(60))) return "overdue";
    if (!now.isBefore(scheduledAt)) return "due";
    return "upcoming";
  }

  public static String painProblem(Integer painScore) {
    if (painScore == null) return null;
    return (painScore < 0 || painScore > 10) ? "درجة الألم بين 0 و10" : null;
  }
}
