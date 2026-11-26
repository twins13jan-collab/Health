package eg.gov.health.platform.patientrights;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ComplaintRulesTest {
  @Test void decisionSeparatedFromInvestigation() {
    assertFalse(ComplaintRules.deciderIndependent("inv@x", "inv@x"));
    assertTrue(ComplaintRules.deciderIndependent("inv@x", "officer@x"));
    assertTrue(ComplaintRules.deciderIndependent(null, "officer@x"));
  }
  @Test void closureNeedsRootCauseAndCorrectiveAction() {
    assertFalse(ComplaintRules.closable("upheld in part", null, "تدريب الفريق"));
    assertFalse(ComplaintRules.closable("upheld in part", "ازدحام الاستقبال", null));
    assertFalse(ComplaintRules.closable(null, "ازدحام", "تدريب"));
    assertTrue(ComplaintRules.closable("upheld in part", "ازدحام الاستقبال", "جدولة إضافية وتدريب"));
  }
  @Test void disclosureNeedsPrivacyAndLegal() {
    assertFalse(ComplaintRules.disclosureApprovable("approved", null));
    assertFalse(ComplaintRules.disclosureApprovable(null, "approved"));
    assertTrue(ComplaintRules.disclosureApprovable("approved", "approved"));
  }
  @Test void anonymousKeptWithoutInventedIdentity() {
    assertTrue(ComplaintRules.anonymousValid(true, null));
    assertTrue(ComplaintRules.anonymousValid(false, "مقدم معروف"));
    assertFalse(ComplaintRules.anonymousValid(false, null));
  }
}
