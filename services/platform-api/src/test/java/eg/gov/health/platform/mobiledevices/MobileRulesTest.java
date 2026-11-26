package eg.gov.health.platform.mobiledevices;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class MobileRulesTest {
  @Test void devicesAreIdentitiesWithDuties() {
    assertFalse(MobileRules.sessionAllowed(false, "active")); // unencrypted
    assertTrue(MobileRules.sessionAllowed(true, "active"));
    assertFalse(MobileRules.sessionAllowed(true, "wiped"));
    assertTrue(MobileRules.updateRequired(104, 105));
    assertFalse(MobileRules.updateRequired(105, 105));
    assertTrue(MobileRules.wipeValid("lost"));
    assertFalse(MobileRules.wipeValid("curiosity"));
  }
}
