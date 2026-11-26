package eg.gov.health.platform.clinical;
import java.time.*; import java.util.*;
/** Emergency-department triage rules (Phase 4): five-level triage with
 *  a maximum target wait per level. The board flags a breach the moment
 *  a waiting patient passes the target for their level — the queue is
 *  ordered by clinical priority, never by arrival alone. */
public final class ErRules {
  private ErRules() {}
  /** Target minutes to first clinician contact, by triage level 1..5. */
  public static final Map<Integer, Integer> TARGET_WAIT_MINUTES = Map.of(1, 0, 2, 10, 3, 60, 4, 120, 5, 240);
  public static boolean validLevel(Integer level) { return level != null && level >= 1 && level <= 5; }
  public static int targetMinutes(int level) { return TARGET_WAIT_MINUTES.getOrDefault(level, 240); }
  public static long waitMinutes(OffsetDateTime arrivedAt, OffsetDateTime now) {
    if (arrivedAt == null || now == null) return 0;
    return Math.max(0, Duration.between(arrivedAt, now).toMinutes());
  }
  /** Breach = still waiting past the level's target. Level 1 never waits. */
  public static boolean breached(Integer level, long waitMinutes, String status) {
    if (!"open".equals(status) || level == null) return false;
    return waitMinutes > targetMinutes(level);
  }
  /** Queue order: waiting first, then triage level, then longest wait. */
  public static Comparator<Map<String, Object>> queueOrder() {
    return (a, b) -> {
      int sa = "open".equals(a.get("status")) ? 0 : "in_progress".equals(a.get("status")) ? 1 : 2;
      int sb = "open".equals(b.get("status")) ? 0 : "in_progress".equals(b.get("status")) ? 1 : 2;
      if (sa != sb) return Integer.compare(sa, sb);
      int la = a.get("triageLevel") instanceof Number n ? n.intValue() : 5;
      int lb = b.get("triageLevel") instanceof Number n ? n.intValue() : 5;
      if (la != lb) return Integer.compare(la, lb);
      long wa = a.get("waitMinutes") instanceof Number n ? n.longValue() : 0;
      long wb = b.get("waitMinutes") instanceof Number n ? n.longValue() : 0;
      return Long.compare(wb, wa);
    };
  }
}
