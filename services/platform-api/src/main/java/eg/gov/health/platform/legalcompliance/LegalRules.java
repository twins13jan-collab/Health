package eg.gov.health.platform.legalcompliance;
import java.time.*;
/** Legal-compliance rules (module 164). A requirement binds
 *  only inside its effective window: not before it enters into
 *  force, and not after it is repealed. An exception to a legal
 *  requirement is a documented, authority-backed, TIME-BOXED
 *  act — an exception without an expiry is a repeal nobody voted
 *  on. A non-compliant review closes only through completed
 *  corrective action. */
public final class LegalRules {
  private LegalRules() {}
  public static boolean requirementApplicable(LocalDate effectiveFrom, LocalDate effectiveTo, LocalDate today) {
    if (effectiveFrom != null && today.isBefore(effectiveFrom)) return false;
    if (effectiveTo != null && today.isAfter(effectiveTo)) return false;
    return true;
  }
  public static boolean exceptionValid(String authorityBasis, LocalDate expiresAt, LocalDate today) {
    return authorityBasis != null && !authorityBasis.isBlank() && expiresAt != null && !expiresAt.isBefore(today);
  }
  public static boolean reviewClosable(boolean compliant, boolean correctiveDone) {
    return compliant || correctiveDone;
  }
}
