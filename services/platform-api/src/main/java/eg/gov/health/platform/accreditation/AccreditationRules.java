package eg.gov.health.platform.accreditation;
import java.time.*;
/** Accreditation rules (module 131). A licence is a date-bound
 *  permission: past its expiry the service is unlicensed,
 *  whatever the framed certificate on the wall says, and a
 *  suspended licence stops the service until the competent
 *  authority lifts it. Inspections with findings close only
 *  when the corrective actions are done, and suspension or
 *  restriction is a decision of a named competent authority —
 *  never an anonymous toggle. */
public final class AccreditationRules {
  private AccreditationRules() {}
  public static boolean licenceValid(String status, LocalDate expiresAt, LocalDate today) {
    return "active".equals(status) && expiresAt != null && today != null && !expiresAt.isBefore(today);
  }
  public static boolean inspectionClosable(String findings, boolean correctiveDone) {
    if (findings == null || findings.isBlank()) return true;
    return correctiveDone;
  }
}
