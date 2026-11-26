package eg.gov.health.platform.recordsdocs;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
class DocumentRulesTest {
  @Test void signatureCountsOnlyWhenValidAndInScope() {
    assertTrue(DocumentRules.signatureCounts(true, true));
    assertFalse(DocumentRules.signatureCounts(false, true)); // invalid signature is not an approval
    assertFalse(DocumentRules.signatureCounts(true, false)); // out of the signer's scope for the type
  }
  @Test void signatureBindsToTheSignedVersionHash() {
    assertTrue(DocumentRules.signatureAppliesTo("sha256:a", "sha256:a"));
    assertFalse(DocumentRules.signatureAppliesTo("sha256:a", "sha256:b")); // edited version: old signature does not carry
  }
  @Test void deletionBlockedByHoldOrRetention() {
    var today = LocalDate.of(2026, 11, 22);
    assertTrue(DocumentRules.deletionBlocked(true, null, today));
    assertTrue(DocumentRules.deletionBlocked(false, LocalDate.of(2031, 1, 1), today));
    assertFalse(DocumentRules.deletionBlocked(false, LocalDate.of(2020, 1, 1), today));
    assertFalse(DocumentRules.deletionBlocked(false, null, today));
  }
  @Test void retentionIsRecordsWork() {
    assertTrue(DocumentRules.mayManageRetention("records_manager"));
    assertFalse(DocumentRules.mayManageRetention("physician"));
  }
}
