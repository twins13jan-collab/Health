package eg.gov.health.platform.nursing;
import java.time.*;
/** Nursing rules (module 85). Tasks are assigned by licence,
 *  competence and workload: a task outside the nurse's licensed
 *  scope (blood administration is the classic case) is refused,
 *  and a nurse already carrying the maximum open tasks is not
 *  silently loaded further — the task is reassigned or escalated.
 *  Overdue tasks surface as overdue; they never quietly disappear
 *  from the board. Shift handover is structured: critical cases
 *  and open tasks are named, and the incoming nurse accepts them. */
public final class NursingRules {
  private NursingRules() {}
  public static final int MAX_OPEN_TASKS = 12;
  public static boolean assignmentPermitted(String nurseScope, String taskType, long openTasks) {
    if (openTasks >= MAX_OPEN_TASKS) return false;
    if ("blood".equals(taskType)) return nurseScope != null && nurseScope.contains("blood");
    return nurseScope != null && !nurseScope.isBlank();
  }
  public static boolean isOverdue(OffsetDateTime dueAt, String status, OffsetDateTime now) {
    return dueAt != null && now != null && now.isAfter(dueAt) && !"done".equals(status);
  }
  public static boolean handoverComplete(String criticalCasesNote, Integer openTasksCount) {
    return criticalCasesNote != null && !criticalCasesNote.isBlank() && openTasksCount != null;
  }
}
