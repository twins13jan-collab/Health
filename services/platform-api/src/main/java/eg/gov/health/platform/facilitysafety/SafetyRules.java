package eg.gov.health.platform.facilitysafety;
import java.time.*;
/** Facility safety & civil-protection rules (module 107). Hot-work
 *  permits are approved by someone OTHER than the requester, welding
 *  requires a fire watch, electrical work requires lockout/tagout,
 *  and work happens only inside the permit's validity window. Access
 *  to sensitive areas is approved, time-boxed, and — for visitors and
 *  contractors — escorted. A failed safety-system inspection is not
 *  closed by recording it: it carries defects and a corrective
 *  action. Evacuation drills measure the evacuation time against
 *  the building plan. */
public final class SafetyRules {
  private SafetyRules() {}
  public static boolean permitApprovable(String requester, String approver, String workType, boolean fireWatch, boolean loto) {
    if (requester == null || approver == null || requester.equals(approver)) return false; // no self-approval
    if ("welding".equals(workType) && !fireWatch) return false;
    if ("electrical_isolation".equals(workType) && !loto) return false;
    return true;
  }
  public static boolean withinValidity(OffsetDateTime from, OffsetDateTime until, OffsetDateTime at) {
    return from != null && until != null && at != null && !at.isBefore(from) && !at.isAfter(until);
  }
  public static boolean accessAllowed(String personType, boolean approved, String escortedBy, OffsetDateTime from, OffsetDateTime until, OffsetDateTime at) {
    if (!approved || !withinValidity(from, until, at)) return false;
    if (("visitor".equals(personType) || "contractor".equals(personType)) && (escortedBy == null || escortedBy.isBlank())) return false;
    return true;
  }
}
