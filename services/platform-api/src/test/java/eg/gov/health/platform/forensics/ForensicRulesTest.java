package eg.gov.health.platform.forensics;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ForensicRulesTest {
  @Test void examinationNeedsVerifiedAuthority() {
    assertTrue(ForensicRules.examinationPermitted(true));
    assertFalse(ForensicRules.examinationPermitted(false));
  }
  @Test void custodyChainNeverBreaks() {
    assertTrue(ForensicRules.transferPermitted("stored", "مخزن الأدلة", "معمل السموم"));
    assertFalse(ForensicRules.transferPermitted("released", "أ", "ب")); // already released: nothing to transfer
    assertFalse(ForensicRules.transferPermitted("stored", null, "ب"));
    assertFalse(ForensicRules.transferPermitted("stored", "أ", " "));
  }
  @Test void reportReleaseNeedsTwoSignaturesAndTheRightAuthority() {
    assertTrue(ForensicRules.reportReleasePermitted("dr-a", "dr-b", "نيابة أسيوط", "نيابة أسيوط"));
    assertFalse(ForensicRules.reportReleasePermitted("dr-a", "dr-a", "نيابة أسيوط", "نيابة أسيوط")); // self-countersign
    assertFalse(ForensicRules.reportReleasePermitted("dr-a", null, "نيابة أسيوط", "نيابة أسيوط"));
    assertFalse(ForensicRules.reportReleasePermitted("dr-a", "dr-b", "صحيفة", "نيابة أسيوط")); // not the requesting authority
    assertFalse(ForensicRules.forensicAccess("receptionist"));
    assertTrue(ForensicRules.forensicAccess("forensic_physician"));
  }
}
