package eg.gov.health.platform.hrdeep;
import java.time.*;
/** HR rules (module 116). Clinical assignment requires a valid
 *  licence — a lapsed licence is not a paperwork detail.
 *  Mandatory training overdue means the staff member is flagged
 *  non-compliant, never silently compliant. And a grievance
 *  closes only after review with a recorded outcome: an
 *  unanswered grievance that quietly expires teaches the whole
 *  workforce that speaking is pointless. */
public final class HrRules {
  private HrRules() {}
  public static boolean assignable(boolean clinical, boolean licenceValid) { return !clinical || licenceValid; }
  public static boolean trainingCompliant(boolean mandatory, boolean completed, LocalDate dueDate, LocalDate day) {
    if (!mandatory) return true;
    return completed || (dueDate != null && day != null && !day.isAfter(dueDate));
  }
  public static boolean grievanceClosable(boolean reviewed, String outcome) {
    return reviewed && outcome != null && !outcome.isBlank();
  }
}
