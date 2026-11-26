package eg.gov.health.platform.diseaseregistries;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class RegistryRulesTest {
  @Test void publicationNeedsCohortAndHonestLoss() {
    assertEquals(40.0, RegistryRules.lostRatePct(50, 20));
    assertFalse(RegistryRules.publishable(20, 5.0, "note"));          // cohort too small
    assertTrue(RegistryRules.publishable(50, 12.0, null));            // modest loss, fine
    assertFalse(RegistryRules.publishable(50, 40.0, null));           // heavy loss, silent -> blocked
    assertTrue(RegistryRules.publishable(50, 40.0, "فقد المتابعة أعلى بالمناطق النائية — تحيز محتمل نحو البقاء"));
  }
}
