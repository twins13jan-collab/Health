package eg.gov.health.platform.schoolhealth;
import java.util.Set;
/** School & university health rules (module 143). Screening and
 *  vaccination of minors stand on guardian consent — recorded on
 *  the record, and for vaccination verified AGAIN at the moment of
 *  administration. An abnormal screening that needs referral is
 *  not complete until the guardian is notified. Institutional
 *  reports are aggregate-only with a minimum group size: under ten
 *  students a "statistic" is a disclosure. Mental-health support is
 *  confidential to counsellor and administration roles. */
public final class SchoolHealthRules {
  private SchoolHealthRules() {}
  public static final int MIN_AGGREGATE_GROUP = 10;
  public static final Set<String> CONFIDENTIAL_ROLES = Set.of("school_counselor", "admin");
  public static boolean screeningPermitted(boolean guardianConsent) { return guardianConsent; }
  public static boolean vaccinationPermitted(boolean guardianConsent, boolean consentVerifiedAtAdministration) {
    return guardianConsent && consentVerifiedAtAdministration;
  }
  public static boolean referralComplete(boolean referralNeeded, boolean guardianNotified) {
    return !referralNeeded || guardianNotified;
  }
  public static boolean aggregatePublishable(long groupSize) { return groupSize >= MIN_AGGREGATE_GROUP; }
  public static boolean confidentialVisible(String eventType, String role) {
    return !"mental_health_support".equals(eventType) || (role != null && CONFIDENTIAL_ROLES.contains(role));
  }
}
