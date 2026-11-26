package eg.gov.health.platform.facilityestablishment;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class EstablishmentRulesTest {
  @Test void openingNeedsEveryGate() {
    var all = Map.of("equipment_installation", true, "equipment_testing", true, "it_systems", true, "staff_training", true, "trial_run", true);
    assertTrue(EstablishmentRules.missingGates(true, true, true, all, true).isEmpty());
    var noTrial = new HashMap<>(all); noTrial.remove("trial_run");
    assertTrue(EstablishmentRules.missingGates(true, true, true, noTrial, true).contains("commissioning:trial_run"));
    assertTrue(EstablishmentRules.missingGates(false, true, true, all, true).contains("needs_study"));
    assertTrue(EstablishmentRules.missingGates(true, true, true, all, false).contains("opening_licence"));
  }
}
