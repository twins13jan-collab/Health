package eg.gov.health.platform.schoolhealth;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class SchoolHealthRulesTest {
  @Test void minorsStandOnGuardianConsent() {
    assertFalse(SchoolHealthRules.screeningPermitted(false));
    assertTrue(SchoolHealthRules.screeningPermitted(true));
    assertFalse(SchoolHealthRules.vaccinationPermitted(true, false)); // recorded consent is not enough at the needle
    assertTrue(SchoolHealthRules.vaccinationPermitted(true, true));
  }
  @Test void abnormalReferralNeedsGuardianNotification() {
    assertFalse(SchoolHealthRules.referralComplete(true, false));
    assertTrue(SchoolHealthRules.referralComplete(true, true));
    assertTrue(SchoolHealthRules.referralComplete(false, false));
  }
  @Test void smallGroupsAreDisclosuresNotStatistics() {
    assertFalse(SchoolHealthRules.aggregatePublishable(4));
    assertTrue(SchoolHealthRules.aggregatePublishable(25));
  }
  @Test void mentalHealthSupportIsConfidential() {
    assertFalse(SchoolHealthRules.confidentialVisible("mental_health_support", "receptionist"));
    assertTrue(SchoolHealthRules.confidentialVisible("mental_health_support", "school_counselor"));
    assertTrue(SchoolHealthRules.confidentialVisible("outbreak", "receptionist"));
  }
}
