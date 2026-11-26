package eg.gov.health.platform.envhealth;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class EnvHealthRulesTest {
  @Test void officialAlertsNeedReliableData() {
    assertFalse(EnvHealthRules.alertPublishable(false));
    assertTrue(EnvHealthRules.alertPublishable(true));
  }
  @Test void emergenciesAndUnsafeWaterNeedRealResponses() {
    assertFalse(EnvHealthRules.alertClosable("emergency", false));
    assertTrue(EnvHealthRules.alertClosable("emergency", true));
    assertTrue(EnvHealthRules.alertClosable("watch", false));
    assertFalse(EnvHealthRules.waterCheckClosable("unsafe", null));
    assertFalse(EnvHealthRules.waterCheckClosable("unsafe", " "));
    assertTrue(EnvHealthRules.waterCheckClosable("unsafe", "boil-water advisory issued"));
    assertTrue(EnvHealthRules.waterCheckClosable("safe", null));
  }
}
