package eg.gov.health.platform.protocolversions;
import java.time.*; import java.util.*;
/** Protocol-version rules (module 61). A protocol version goes
 *  live only complete: scientific reference, owning body,
 *  approving committee and effective date — dosing and
 *  interaction rules are governed content, never undocumented
 *  logic buried in code. Every clinical service links to the
 *  version in force at the time it was delivered, and the
 *  register can always answer which protocol governed a past
 *  service. A withdrawn version governs nothing new. */
public final class ProtocolRules {
  private ProtocolRules() {}
  public static List<String> activationBlockers(String scientificRef, String ownerBody, boolean committeeApproved, LocalDate effectiveFrom) {
    List<String> b = new ArrayList<>();
    if (scientificRef == null || scientificRef.isBlank()) b.add("scientific_ref_missing");
    if (ownerBody == null || ownerBody.isBlank()) b.add("owner_body_missing");
    if (!committeeApproved) b.add("committee_unapproved");
    if (effectiveFrom == null) b.add("effective_from_missing");
    return b;
  }
  public static boolean inForce(String status, LocalDate effectiveFrom, LocalDate serviceDate) {
    return "active".equals(status) && effectiveFrom != null && serviceDate != null && !serviceDate.isBefore(effectiveFrom);
  }
}
