package eg.gov.health.platform.capabilityaccess;
import java.time.*; import java.util.*;
/** Capability-availability rules (module 160). A service is
 *  available only when its capability is real right now: staff,
 *  equipment, licence and linked protocol all in place, and the
 *  verification fresh (30 days). A service name printed in a
 *  directory is not availability — announcing a service the
 *  facility cannot actually deliver sends patients travelling
 *  to a locked door. When unavailable, the reason and the last
 *  verification are shown, never hidden. */
public final class CapabilityRules {
  private CapabilityRules() {}
  public static final long FRESH_DAYS = 30;
  public static List<String> missingPillars(boolean staff, boolean equipment, boolean licence, boolean protocol) {
    List<String> m = new ArrayList<>();
    if (!staff) m.add("staff_not_ready");
    if (!equipment) m.add("equipment_not_ready");
    if (!licence) m.add("licence_invalid");
    if (!protocol) m.add("protocol_not_linked");
    return m;
  }
  public static boolean fresh(OffsetDateTime lastVerified, OffsetDateTime now) {
    return lastVerified != null && now != null && !lastVerified.isBefore(now.minusDays(FRESH_DAYS));
  }
  public static boolean available(boolean staff, boolean equipment, boolean licence, boolean protocol,
                                  OffsetDateTime lastVerified, OffsetDateTime now) {
    return missingPillars(staff, equipment, licence, protocol).isEmpty() && fresh(lastVerified, now);
  }
}
