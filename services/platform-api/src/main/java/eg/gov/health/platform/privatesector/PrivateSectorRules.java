package eg.gov.health.platform.privatesector;
import java.time.*;
/** Private-sector oversight rules. A private facility's licence is
 *  its permission to operate on the platform: expired licences top
 *  the oversight board, licences expiring within LICENSE_WARN_DAYS
 *  are flagged for renewal follow-up, and a facility that never
 *  recorded licence data is visible as such — never silently
 *  assumed licensed. Facility types group into the management
 *  families the sector is run by: hospitals, clinics, laboratories,
 *  pharmacies, medical centres, and everything else. */
public final class PrivateSectorRules {
  private PrivateSectorRules() {}
  public static final long LICENSE_WARN_DAYS = 90;
  public static String licenseState(LocalDate expiry, LocalDate today) {
    if (expiry == null || today == null) return "not_recorded";
    if (today.isAfter(expiry)) return "expired";
    if (!expiry.isAfter(today.plusDays(LICENSE_WARN_DAYS))) return "expiring_soon";
    return "valid";
  }
  /** Expired licences first, then pending approvals, then renewals
   *  coming due, then unrecorded licences, then the healthy rest. */
  public static int attentionRank(String status, String licenseState) {
    if ("expired".equals(licenseState)) return 0;
    if ("pending".equals(status)) return 1;
    if ("expiring_soon".equals(licenseState)) return 2;
    if ("not_recorded".equals(licenseState)) return 3;
    return 4;
  }
  public static String typeGroup(String type) {
    if (type == null) return "other";
    return switch (type) {
      case "hospital", "general_hospital", "university_hospital", "emergency_hospital" -> "hospitals";
      case "clinic" -> "clinics";
      case "lab", "laboratory" -> "labs";
      case "pharmacy" -> "pharmacies";
      case "center", "specialized_center", "radiology_center", "health_center", "health_unit", "family_center" -> "centers";
      default -> "other";
    };
  }
}
