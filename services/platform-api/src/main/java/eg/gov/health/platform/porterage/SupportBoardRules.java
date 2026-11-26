package eg.gov.health.platform.porterage;
import java.time.*;
/** Attention ordering for the support-services board (module 109
 *  operations): a transport that has already breached its response
 *  target outranks everything, then critical transports, then
 *  patients already picked up (they are mid-journey), then the
 *  rest by priority and age. A room discharged more than
 *  ROOM_TARGET_MINUTES ago and still not ready is overdue — beds
 *  blocked by turnover are beds denied to the ER and ADT queues.
 *  Laundry states name losses openly. Pure logic. */
public final class SupportBoardRules {
  private SupportBoardRules() {}
  public static final int ROOM_TARGET_MINUTES = 120;

  public static int priorityRank(String priority) {
    if (priority == null) return 3;
    return switch (priority) { case "critical" -> 0; case "urgent" -> 1; case "routine" -> 2; default -> 3; };
  }
  public static boolean isOpen(String transportStatus) {
    return "requested".equals(transportStatus) || "assigned".equals(transportStatus) || "picked_up".equals(transportStatus);
  }
  /** Waiting beyond the priority's response target while still open. */
  public static boolean waitBreached(String priority, OffsetDateTime requestedAt, OffsetDateTime now) {
    if (requestedAt == null || now == null) return false;
    long wait = Duration.between(requestedAt, now).toMinutes();
    return wait > PorterageRules.TARGET_MIN.getOrDefault(priority == null ? "routine" : priority, 45);
  }
  public static int transportRank(String status, String priority, boolean breached) {
    if (!isOpen(status)) return 9;
    if (breached) return 0;
    if ("critical".equals(priority)) return 1;
    if ("picked_up".equals(status)) return 2;
    return 3 + priorityRank(priority);
  }
  public static long waitMinutes(OffsetDateTime from, OffsetDateTime now) {
    if (from == null || now == null) return -1;
    return Duration.between(from, now).toMinutes();
  }
  public static boolean roomOverdue(String status, OffsetDateTime dischargedAt, OffsetDateTime now) {
    if ("ready".equals(status) || dischargedAt == null || now == null) return false;
    return waitMinutes(dischargedAt, now) > ROOM_TARGET_MINUTES;
  }
  public static String laundryState(String status, Integer sent, Integer returned) {
    // LaundryBatch.receive() closes a batch with status "returned".
    if (!"returned".equals(status) && !"received".equals(status)) return "in_process";
    int loss = PorterageRules.laundryLoss(sent == null ? 0 : sent, returned);
    return loss > 0 ? "received_with_loss" : "received_complete";
  }
}
