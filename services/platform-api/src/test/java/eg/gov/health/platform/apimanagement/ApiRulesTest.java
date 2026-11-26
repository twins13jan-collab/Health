package eg.gov.health.platform.apimanagement;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ApiRulesTest {
  @Test void breakingVersionsNeedADeprecationWindow() {
    assertTrue(ApiRules.versionPublishable(true, false));
    assertFalse(ApiRules.versionPublishable(false, false));
    assertTrue(ApiRules.versionPublishable(false, true));
  }
  @Test void revokedKeysAndExhaustedQuotasDeny() {
    assertFalse(ApiRules.consumerAllowed("revoked", 10, 1000));
    assertFalse(ApiRules.consumerAllowed("active", 1000, 1000));
    assertTrue(ApiRules.consumerAllowed("active", 999, 1000));
  }
}
