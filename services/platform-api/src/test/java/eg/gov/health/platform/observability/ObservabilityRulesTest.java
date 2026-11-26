package eg.gov.health.platform.observability;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ObservabilityRulesTest {
  @Test void breachesSurfaceAndCriticalsEscalate() {
    assertTrue(ObservabilityRules.breached(950.0, 500.0));
    assertFalse(ObservabilityRules.breached(320.0, 500.0));
    assertFalse(ObservabilityRules.closable("minor", "open", null));
    assertTrue(ObservabilityRules.closable("minor", "open", "أعيد تشغيل الخدمة"));
    assertFalse(ObservabilityRules.closable("critical", "open", "أعيد تشغيل الخدمة"));
    assertTrue(ObservabilityRules.closable("critical", "escalated", "أعيد التشغيل ورُوجع السبب"));
  }
}
