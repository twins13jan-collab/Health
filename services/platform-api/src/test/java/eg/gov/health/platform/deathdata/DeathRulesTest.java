package eg.gov.health.platform.deathdata;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class DeathRulesTest {
  @Test void certifiedCausesAndHistoricalCorrections() {
    assertFalse(DeathRules.statisticsReady("I21", false));
    assertFalse(DeathRules.statisticsReady("", true));
    assertTrue(DeathRules.statisticsReady("I21.9", true));
    assertFalse(DeathRules.correctionValid("place", "عناية مركزة", ""));
    assertTrue(DeathRules.correctionValid("place", "عناية مركزة", "تعارض مع سجل الدخول"));
  }
}
