package eg.gov.health.platform.servicecatalog;
import java.time.*;
/** Service-catalog rules (module 78). One national code, one
 *  definition — a duplicate code is a conflict, not a second
 *  truth. A service activates only with its resource
 *  requirements and delivery location defined and an effective
 *  window set; prices and coverage are administrative data,
 *  kept separate from the clinical definition itself. Changes
 *  are approved and take effect from their effective date,
 *  never retroactively by editing history. */
public final class CatalogRules {
  private CatalogRules() {}
  public static boolean activatable(String resourceRequirements, String deliveryLocation, LocalDate effectiveFrom) {
    return resourceRequirements != null && !resourceRequirements.isBlank()
        && deliveryLocation != null && !deliveryLocation.isBlank() && effectiveFrom != null;
  }
  public static boolean applicable(String status, LocalDate from, LocalDate to, LocalDate day) {
    if (!"active".equals(status) || day == null) return false;
    if (from != null && day.isBefore(from)) return false;
    return to == null || !day.isAfter(to);
  }
}
