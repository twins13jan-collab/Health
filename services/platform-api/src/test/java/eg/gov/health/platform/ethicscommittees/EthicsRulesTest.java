package eg.gov.health.platform.ethicscommittees;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class EthicsRulesTest {
  @Test void decisionsNeedQuorumAndDeclaredInterests() {
    assertTrue(EthicsRules.quorumMet(5, 4));
    assertFalse(EthicsRules.quorumMet(3, 4));
    assertFalse(EthicsRules.quorumMet(null, 4));
    assertTrue(EthicsRules.votingPermitted(true));
    assertFalse(EthicsRules.votingPermitted(false));
  }
}
