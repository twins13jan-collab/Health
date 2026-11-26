package eg.gov.health.platform.medicalassets;
import java.time.*; import java.util.Set;
/** Medical asset rules (cycle 230). Every asset has an identity, an
 *  owner, a location and a state. Maintenance and calibration are
 *  verified BEFORE use: an out-of-calibration device is flagged unfit
 *  at integration time. The clinical user reports a fault but never
 *  returns the device to service — biomedical engineering and quality
 *  release it, each within their competence. A patient-affecting fault
 *  opens a linked safety case. A missing spare part spawns a purchase
 *  request and ties the downtime to inventory. */
public final class AssetRules {
  private AssetRules() {}
  public static final Set<String> RELEASE_ROLES = Set.of("biomedical_engineer","quality_officer","admin");
  public static boolean mayRelease(String role) { return role != null && RELEASE_ROLES.contains(role); }
  public static boolean calibrationCurrent(LocalDate due, LocalDate today) {
    return due != null && today != null && !today.isAfter(due);
  }
  public static boolean usable(String status, LocalDate calibrationDue, LocalDate today) {
    return "in_service".equals(status) && calibrationCurrent(calibrationDue, today);
  }
  public static boolean safetyCaseRequired(boolean patientAffected, String safetyCaseRef) {
    return !patientAffected || (safetyCaseRef != null && !safetyCaseRef.isBlank());
  }
}
