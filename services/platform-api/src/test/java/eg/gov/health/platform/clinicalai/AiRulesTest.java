package eg.gov.health.platform.clinicalai;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
class AiRulesTest {
  @Test void approvalIsCommitteeWork() {
    assertTrue(AiRules.mayApproveClinicalUse("clinical_ai_committee"));
    assertTrue(AiRules.mayApproveClinicalUse("chief_medical_officer"));
    assertFalse(AiRules.mayApproveClinicalUse("data_scientist")); // owner never approves alone
    assertTrue(AiRules.inferable("approved", true));
    assertFalse(AiRules.inferable("validated", true));
    assertFalse(AiRules.inferable("suspended", true));
  }
  @Test void abstentionOnIncompleteOrOutOfScope() {
    assertTrue(AiRules.mustAbstain(false, true));
    assertTrue(AiRules.mustAbstain(true, false));
    assertFalse(AiRules.mustAbstain(true, true));
  }
  @Test void driftSuspendsAtThreshold() {
    assertFalse(AiRules.driftSuspends(new BigDecimal("0.05")));
    assertTrue(AiRules.driftSuspends(new BigDecimal("0.10")));
    assertTrue(AiRules.driftSuspends(new BigDecimal("0.22")));
  }
  @Test void onlyHumanAcceptOrModifyApplies() {
    assertTrue(AiRules.applicable("accepted"));
    assertTrue(AiRules.applicable("modified"));
    assertFalse(AiRules.applicable("rejected"));
    assertFalse(AiRules.applicable(null)); // no automated effect
  }
}
