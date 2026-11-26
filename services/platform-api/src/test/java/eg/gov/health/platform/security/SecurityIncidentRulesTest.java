package eg.gov.health.platform.security;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class SecurityIncidentRulesTest {
  @Test void evidenceNeedsSha256() {
    assertTrue(SecurityIncidentRules.evidenceValid("a".repeat(64)));
    assertFalse(SecurityIncidentRules.evidenceValid("deadbeef"));
    assertFalse(SecurityIncidentRules.evidenceValid(null));
  }
  @Test void compromisedAccountNeedsRevocation() {
    assertFalse(SecurityIncidentRules.containmentValid("compromised_account", false));
    assertTrue(SecurityIncidentRules.containmentValid("compromised_account", true));
    assertTrue(SecurityIncidentRules.containmentValid("data_leak", false));
  }
  @Test void ransomwareNeedsVerifiedCleanBackup() {
    assertFalse(SecurityIncidentRules.recoveryValid("ransomware", false));
    assertTrue(SecurityIncidentRules.recoveryValid("ransomware", true));
    assertTrue(SecurityIncidentRules.recoveryValid("other", false));
  }
  @Test void privacyRolesAndClosure() {
    assertTrue(SecurityIncidentRules.mayAssessPrivacy("privacy_officer"));
    assertFalse(SecurityIncidentRules.mayAssessPrivacy("receptionist"));
    assertTrue(SecurityIncidentRules.notificationDecisionValid("خطر مؤكد على الأشخاص"));
    assertFalse(SecurityIncidentRules.notificationDecisionValid(" "));
    assertFalse(SecurityIncidentRules.closable(true, true, true, false));
    assertTrue(SecurityIncidentRules.closable(true, true, true, true));
    assertFalse(SecurityIncidentRules.closable(false, true, true, true));
  }
}
