package eg.gov.health.platform.transfersafety;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class TransferSafetyRulesTest {
  @Test void departureNeedsAllThreeConfirmations() {
    assertTrue(TransferSafetyRules.departurePermitted(true, true, true));
    assertFalse(TransferSafetyRules.departurePermitted(true, false, true)); // not accepted
    assertFalse(TransferSafetyRules.departurePermitted(true, true, false)); // no capacity
    assertFalse(TransferSafetyRules.departurePermitted(false, true, true)); // stability unconfirmed
  }
  @Test void unstablePatientsNeedPhysicianLevelEscort() {
    assertTrue(TransferSafetyRules.escortAdequate(false, "physician"));
    assertTrue(TransferSafetyRules.escortAdequate(false, "icu_team"));
    assertFalse(TransferSafetyRules.escortAdequate(false, "nurse"));
    assertFalse(TransferSafetyRules.escortAdequate(false, "none"));
    assertTrue(TransferSafetyRules.escortAdequate(true, "nurse"));
  }
}
