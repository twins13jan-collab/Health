package eg.gov.health.platform.softwarelifecycle;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class LifecycleRulesTest {
  @Test void productionIsAPromiseKeptByGates() {
    assertTrue(LifecycleRules.productionDeployable(true, true, false, false, null));
    assertFalse(LifecycleRules.productionDeployable(false, true, false, false, null)); // unreviewed
    assertFalse(LifecycleRules.productionDeployable(true, false, false, false, null)); // unsigned
    assertFalse(LifecycleRules.productionDeployable(true, true, true, false, null));   // config drift
    assertFalse(LifecycleRules.productionDeployable(true, true, false, true, null));   // third-party, no licence
    assertTrue(LifecycleRules.productionDeployable(true, true, false, true, "LIC-2026-114"));
    assertFalse(LifecycleRules.rollbackValid(""));
    assertTrue(LifecycleRules.rollbackValid("تسرب ذاكرة بالعناية"));
  }
}
