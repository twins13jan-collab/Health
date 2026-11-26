package eg.gov.health.platform.porterage;
import java.time.*; import java.util.Map;
/** Internal transport & hotel-services rules (module 109). Critical
 *  transports are prioritized and never move without their clinical
 *  escort — and oxygen-dependent patients never move without an
 *  escort either. Response time is measured per priority against a
 *  target and breaches are flagged, not averaged away. A room is
 *  ready for the next patient only when cleaning is done, the bed
 *  is made AND an inspection signed. Laundry losses are counted,
 *  never silently absorbed. */
public final class PorterageRules {
  private PorterageRules() {}
  public static final Map<String,Integer> TARGET_MIN = Map.of("critical", 10, "urgent", 20, "routine", 45);
  public static boolean escortSatisfied(String priority, boolean oxygenRequired, String escortName) {
    boolean needed = "critical".equals(priority) || oxygenRequired;
    return !needed || (escortName != null && !escortName.isBlank());
  }
  public static long responseMinutes(OffsetDateTime requested, OffsetDateTime delivered) {
    if (requested == null || delivered == null) return -1;
    return Duration.between(requested, delivered).toMinutes();
  }
  public static boolean responseBreached(String priority, OffsetDateTime requested, OffsetDateTime delivered) {
    long m = responseMinutes(requested, delivered);
    return m >= 0 && m > TARGET_MIN.getOrDefault(priority == null ? "routine" : priority, 45);
  }
  public static boolean roomReady(boolean cleaningDone, boolean bedReady, String inspectedBy) {
    return cleaningDone && bedReady && inspectedBy != null && !inspectedBy.isBlank();
  }
  public static int laundryLoss(int sent, Integer returned) {
    return returned == null ? 0 : Math.max(0, sent - returned);
  }
}
