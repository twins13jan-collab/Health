package eg.gov.health.platform.unknownpatient;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class UnknownPatientRulesTest {
  @Test void mergeNeedsEvidenceAndIndependentVerifier() {
    assertTrue(UnknownPatientRules.mergeAllowed("active", "بصمة مطابقة + شهادة قريب", "officer@x", "nurse@x"));
    assertFalse(UnknownPatientRules.mergeAllowed("active", null, "officer@x", "nurse@x")); // no evidence
    assertFalse(UnknownPatientRules.mergeAllowed("active", "أدلة", "nurse@x", "nurse@x")); // creator verifying own case
    assertFalse(UnknownPatientRules.mergeAllowed("merged", "أدلة", "officer@x", "nurse@x")); // already merged
  }
  @Test void protectionDataIsRoleRestricted() {
    assertFalse(UnknownPatientRules.protectionVisible(true, "receptionist"));
    assertTrue(UnknownPatientRules.protectionVisible(true, "social_worker"));
    assertTrue(UnknownPatientRules.protectionVisible(true, "admin"));
    assertTrue(UnknownPatientRules.protectionVisible(false, "receptionist"));
  }
}
