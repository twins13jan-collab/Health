package eg.gov.health.platform.supplychaindeep;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class SupplyRulesTest {
  @Test void dependenceAlternativesAndFloors() {
    assertFalse(SupplyRules.highDependence(60));
    assertTrue(SupplyRules.highDependence(85));
    assertFalse(SupplyRules.riskManaged(true, false));
    assertTrue(SupplyRules.riskManaged(true, true));
    assertTrue(SupplyRules.riskManaged(false, false));
    assertEquals(40, SupplyRules.emergencyShortfall(100, 60));
    assertEquals(0, SupplyRules.emergencyShortfall(100, 140));
  }
}
