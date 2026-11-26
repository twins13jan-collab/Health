package eg.gov.health.platform.diagnostics;
import java.util.Comparator;
/** Radiology worklist rules (module 93, Phase 4.8). The department
 *  runs two queues: acquisition (ordered / scheduled / in progress —
 *  the technologist worklist) and reporting (imaged, awaiting a
 *  radiologist report). Both are ordered by clinical priority first
 *  and waiting time second, so a stat exam never waits behind a
 *  routine one. Reporting carries a turnaround SLA per priority;
 *  an imaged study past its SLA is flagged overdue on the board —
 *  it ages in the open, never silently. */
public final class RadiologyBoardRules {
  private RadiologyBoardRules() {}
  public static int priorityRank(String priority) {
    if (priority == null) return 3;
    return switch (priority.toLowerCase()) {
      case "stat", "emergency" -> 0;
      case "urgent" -> 1;
      case "routine" -> 2;
      default -> 3;
    };
  }
  public static String stageOf(String status) {
    if (status == null) return "acquisition";
    return switch (status) {
      case "ordered", "scheduled", "in_progress" -> "acquisition";
      case "imaged" -> "reporting";
      case "reported" -> "verification";
      default -> "done";
    };
  }
  public static long reportingSlaMinutes(String priority) {
    return switch (priorityRank(priority)) {
      case 0 -> 60;
      case 1 -> 240;
      default -> 1440;
    };
  }
  public static boolean reportOverdue(String status, String priority, long minutesSinceImaged) {
    return "imaged".equals(status) && minutesSinceImaged > reportingSlaMinutes(priority);
  }
  public static Comparator<RadiologyOrder> acquisitionOrder() {
    return Comparator.comparingInt((RadiologyOrder o) -> priorityRank(o.getPriority()))
        .thenComparing(RadiologyOrder::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder()));
  }
  public static Comparator<RadiologyOrder> reportingOrder() {
    return Comparator.comparingInt((RadiologyOrder o) -> priorityRank(o.getPriority()))
        .thenComparing(RadiologyOrder::getUpdatedAt, Comparator.nullsLast(Comparator.naturalOrder()));
  }
}
