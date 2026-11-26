package eg.gov.health.platform.dataquality;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class DataQualityRulesTest {
  @Test void confidenceBlendsQualityAndFreshness() {
    assertEquals(100.0, DataQualityRules.confidence(100.0, 100.0, 100.0, 0.0));
    assertTrue(DataQualityRules.confidence(60.0, 55.0, 50.0, 30.0) < 70.0); // mediocre + stale
    assertFalse(DataQualityRules.publishable(55.0, null));
    assertTrue(DataQualityRules.publishable(55.0, "اكتمال منخفض بمناطق نائية — قيد التحسين"));
    assertTrue(DataQualityRules.publishable(85.0, null));
  }
}
