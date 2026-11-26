package eg.gov.health.platform.hrdeep;
import java.time.*;
/** Attention rules for the workforce board (module 116 operations
 *  layer, cycle 231). A clinician whose licence has lapsed is not
 *  assignable — that tops the queue, above overdue mandatory
 *  training, which is flagged non-compliance and never read as
 *  silent compliance. A grievance that has not been reviewed
 *  outranks one awaiting its recorded outcome, and nothing about
 *  a grievance expires quietly. */
public final class WorkforceBoardRules {
  private WorkforceBoardRules() {}
  public static final int TRAINING_DUE_SOON_DAYS = 30;

  public static String employeeState(boolean clinical, boolean licenceValid, int overdueMandatory) {
    if (clinical && !licenceValid) return "licence_blocked";
    if (overdueMandatory > 0) return "training_overdue";
    return "compliant";
  }
  public static int employeeRank(String state) {
    return switch (state) { case "licence_blocked" -> 0; case "training_overdue" -> 1; default -> 9; };
  }
  public static String trainingState(boolean mandatory, boolean completed, LocalDate dueDate, LocalDate today) {
    if (completed) return "completed";
    if (!mandatory) return "open";
    if (dueDate == null) return "open";
    if (today.isAfter(dueDate)) return "overdue";
    if (!dueDate.isAfter(today.plusDays(TRAINING_DUE_SOON_DAYS))) return "due_soon";
    return "open";
  }
  public static int trainingRank(String state) {
    return switch (state) { case "overdue" -> 0; case "due_soon" -> 1; case "open" -> 2; default -> 9; };
  }
  public static int grievanceRank(String status, boolean reviewed) {
    if ("closed".equals(status)) return 9;
    return reviewed ? 1 : 0;
  }
}
