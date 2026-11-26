package eg.gov.health.platform.facilityenvironment;
import java.time.*; import java.util.Set;
/** Hygiene, environment & medical-waste rules (module 106). Cleaning
 *  plans are risk-based by area. Regulated waste (infectious, sharps,
 *  chemical, pharmaceutical, pathological) moves only through a
 *  contractor whose license is valid on the collection date, with a
 *  complete chain of custody — generator, collector, receiver — and
 *  disposal never precedes collection. Infectious waste stored
 *  beyond 48 hours before collection is a breach. Environmental
 *  checks (air, water, pests) record measured values against their
 *  standard; a failed check carries an action. Hazardous spills open
 *  a timed response that closes only on containment AND
 *  decontamination. */
public final class WasteRules {
  private WasteRules() {}
  public static final Set<String> REGULATED = Set.of("infectious","sharps","chemical","pharmaceutical","pathological");
  public static final long INFECTIOUS_STORAGE_LIMIT_H = 48;
  public static boolean contractorLicensed(String wasteType, String license, LocalDate licenseValidUntil, LocalDate collectionDate) {
    if (!REGULATED.contains(wasteType)) return true; // general waste needs no medical-waste license
    return license != null && !license.isBlank() && licenseValidUntil != null && collectionDate != null
      && !licenseValidUntil.isBefore(collectionDate);
  }
  public static boolean chainComplete(String generatedBy, String collectedBy, String receivedBy) {
    return generatedBy != null && collectedBy != null && receivedBy != null;
  }
  public static boolean storageBreached(String wasteType, OffsetDateTime generatedAt, OffsetDateTime collectedAt) {
    if (!"infectious".equals(wasteType) || generatedAt == null || collectedAt == null) return false;
    return Duration.between(generatedAt, collectedAt).toHours() > INFECTIOUS_STORAGE_LIMIT_H;
  }
  public static boolean spillClosable(OffsetDateTime containedAt, boolean decontaminated) {
    return containedAt != null && decontaminated;
  }
}
