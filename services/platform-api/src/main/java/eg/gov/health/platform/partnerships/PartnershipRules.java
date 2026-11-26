package eg.gov.health.platform.partnerships;
import java.time.*;
/** Partnership & external-provider rules (module 132). A contract
 *  activates only with privacy AND security terms agreed, a defined
 *  access scope and purpose, and a valid date range. Access is
 *  granted only under an active contract, is purpose-bound and
 *  time-boxed, and dies with the relationship: terminating a partner
 *  revokes every grant. Performance is reviewed against the SLA —
 *  a score under 80 or repeated incidents flags the partner for
 *  action — and conflicts of interest and ownership changes are on
 *  the record. */
public final class PartnershipRules {
  private PartnershipRules() {}
  public static boolean contractActivatable(boolean privacyTerms, boolean securityTerms, String accessScope, String purpose,
                                            LocalDate startsOn, LocalDate endsOn) {
    return privacyTerms && securityTerms
      && accessScope != null && !accessScope.isBlank()
      && purpose != null && !purpose.isBlank()
      && startsOn != null && endsOn != null && endsOn.isAfter(startsOn);
  }
  public static boolean grantPermitted(String partnerStatus, String contractStatus, OffsetDateTime expiresAt, OffsetDateTime now) {
    return "active".equals(partnerStatus) && "active".equals(contractStatus)
      && expiresAt != null && now != null && expiresAt.isAfter(now);
  }
  public static boolean reviewFlagsAction(double slaScore, int incidents) {
    return slaScore < 80.0 || incidents >= 3;
  }
}
