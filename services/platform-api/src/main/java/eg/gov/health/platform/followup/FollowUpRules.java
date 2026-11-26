package eg.gov.health.platform.followup;
import java.time.*;
/** Post-discharge follow-up rules (module 184). Every discharge
 *  plan names a responsible owner. Tasks run on due dates: a task
 *  past due and undone is missed, and missed is escalated, never
 *  silently dropped; a patient unreachable after three documented
 *  attempts escalates too. Results that arrive after discharge are
 *  reviewed — an abnormal result without documented patient
 *  notification is an open safety item. High-risk medication checks
 *  are part of the plan, and a plan cannot close while escalated
 *  tasks or unreviewed abnormal results remain. */
public final class FollowUpRules {
  private FollowUpRules() {}
  public static String taskState(String status, OffsetDateTime dueAt, OffsetDateTime now, int contactAttempts) {
    if ("done".equals(status)) return "done";
    if (contactAttempts >= 3) return "unreachable";
    if (dueAt != null && now != null && now.isAfter(dueAt)) return "escalated";
    return status == null ? "pending" : status;
  }
  public static boolean abnormalNotificationBreach(boolean abnormal, boolean reviewed, boolean patientNotified) {
    return abnormal && reviewed && !patientNotified;
  }
  public static boolean closable(int openEscalatedTasks, int unreviewedResults, int openUnreachable) {
    return openEscalatedTasks == 0 && unreviewedResults == 0 && openUnreachable == 0;
  }
}
