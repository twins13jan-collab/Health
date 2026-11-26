package eg.gov.health.platform.childhealth;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ChildHealthRulesTest {
  @Test void coldChainAndAefiGates() {
    assertFalse(ChildHealthRules.doseValid(false));
    assertTrue(ChildHealthRules.doseValid(true));
    assertFalse(ChildHealthRules.nextDosePermitted(true, false));
    assertTrue(ChildHealthRules.nextDosePermitted(true, true));
    assertTrue(ChildHealthRules.nextDosePermitted(false, false));
  }
}
