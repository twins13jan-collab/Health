package eg.gov.health.platform.anomalyregistry;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class RegistryRulesTest {
  @Test void causalClaimsNeedDocumentedEvidence() {
    assertTrue(RegistryRules.causalClaimPermitted(null, null));
    assertTrue(RegistryRules.causalClaimPermitted("ارتباط محتمل بعامل بيئي", "دراسة وبائية منشورة 2025"));
    assertFalse(RegistryRules.causalClaimPermitted("المصنع القريب هو السبب", null));
    assertFalse(RegistryRules.causalClaimPermitted("المصنع القريب هو السبب", "  "));
  }
  @Test void linkageIsPermissionBound() {
    assertTrue(RegistryRules.linkagePermitted("admin"));
    assertTrue(RegistryRules.linkagePermitted("registry_officer"));
    assertFalse(RegistryRules.linkagePermitted("receptionist"));
  }
}
