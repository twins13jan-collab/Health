package eg.gov.health.platform.diagnostics;
import org.junit.jupiter.api.Test; import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
class CriticalValueRulesTest {
  @Test void potassiumExtremesAreCritical() {
    assertTrue(CriticalValueRules.isCritical("K", new BigDecimal("2.5")));
    assertTrue(CriticalValueRules.isCritical("K", new BigDecimal("6.4")));
    assertFalse(CriticalValueRules.isCritical("K", new BigDecimal("4.2")));
  }
  @Test void unknownTestIsNeverAutoCritical() { assertFalse(CriticalValueRules.isCritical("XYZ", new BigDecimal("9999"))); }
  @Test void glucoseLowIsCritical() { assertTrue(CriticalValueRules.isCritical("GLU", new BigDecimal("35"))); }
}
