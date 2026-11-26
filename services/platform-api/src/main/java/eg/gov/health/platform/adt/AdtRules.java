package eg.gov.health.platform.adt;
import java.time.*; import java.util.*;
/** Pure ADT safety rules (Phase 4): bed occupancy, discharge and
 *  transfer invariants. A bed is a physical resource — it can never
 *  be double-booked, and a stay can never be discharged twice. */
public final class AdtRules {
  private AdtRules() {}
  public static final Set<String> DISPOSITIONS = Set.of("home", "transfer_out", "deceased", "ama", "rehab");
  public static final Set<String> SOURCES = Set.of("er", "clinic", "referral", "direct", "transfer_in");

  /** A bed label is free when no active admission in the ward holds it. */
  public static boolean bedFree(Collection<String> occupiedBedLabels, String bedLabel) {
    if (bedLabel == null || bedLabel.isBlank()) return false;
    return occupiedBedLabels.stream().noneMatch(b -> b.equalsIgnoreCase(bedLabel.trim()));
  }
  /** The ward has physical capacity left for one more admission. */
  public static boolean wardHasCapacity(int totalBeds, int activeAdmissions) {
    return totalBeds > 0 && activeAdmissions < totalBeds;
  }
  public static boolean validDisposition(String disposition) {
    return disposition != null && DISPOSITIONS.contains(disposition);
  }
  public static boolean validSource(String source) {
    return source == null || SOURCES.contains(source);
  }
  /** Transfer needs a reason and a genuinely different location. */
  public static String transferProblem(UUID fromWard, String fromBed, UUID toWard, String toBed, String reason) {
    if (reason == null || reason.isBlank()) return "transfer_reason_required";
    if (toWard == null || toBed == null || toBed.isBlank()) return "transfer_target_required";
    if (toWard.equals(fromWard) && toBed.trim().equalsIgnoreCase(fromBed == null ? "" : fromBed.trim()))
      return "transfer_same_bed";
    return null;
  }
  public static boolean dischargeable(String status) { return "admitted".equals(status); }
  /** Whole days of stay, minimum 0 — used for the census board. */
  public static long lengthOfStayDays(OffsetDateTime admittedAt, OffsetDateTime at) {
    if (admittedAt == null || at == null) return 0;
    return Math.max(0, Duration.between(admittedAt, at).toDays());
  }
}
