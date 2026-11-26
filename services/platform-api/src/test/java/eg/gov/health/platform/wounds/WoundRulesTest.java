package eg.gov.health.platform.wounds;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class WoundRulesTest {
  @Test void areaAndHealingVerdicts() {
    assertEquals(20.0, WoundRules.areaCm2(5.0, 4.0));
    assertEquals(-50.0, WoundRules.areaChangePercent(20.0, 10.0));
    assertEquals("improving", WoundRules.healingVerdict(20.0, 10.0, false));
    assertEquals("healed", WoundRules.healingVerdict(20.0, 0.0, false));
    assertEquals("static_continue_plan", WoundRules.healingVerdict(20.0, 19.0, false));
    assertEquals("deteriorating_escalate", WoundRules.healingVerdict(20.0, 25.0, false));
  }
  @Test void infectionEscalatesRegardless() {
    assertEquals("infection_suspected_escalate", WoundRules.healingVerdict(20.0, 10.0, true));
  }
  @Test void parklandFormula() {
    assertEquals(8400.0, WoundRules.parklandMl24h(70.0, 30.0));
    assertEquals(4200.0, WoundRules.parklandFirst8h(70.0, 30.0));
    assertTrue(WoundRules.tbsaValid(45.0));
    assertFalse(WoundRules.tbsaValid(140.0));
  }
  @Test void imageConsentAndMaterialGates() {
    assertTrue(WoundRules.imageConsentValid("patient_consent"));
    assertFalse(WoundRules.imageConsentValid("family_ok"));
    assertFalse(WoundRules.imageConsentValid(null));
    assertTrue(WoundRules.mayViewImage("privacy_officer"));
    assertFalse(WoundRules.mayViewImage("receptionist"));
    assertTrue(WoundRules.materialUsable("ok"));
    assertFalse(WoundRules.materialUsable("expired"));
    assertFalse(WoundRules.materialUsable("recalled"));
  }
}
