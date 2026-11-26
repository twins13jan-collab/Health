package eg.gov.health.platform.institutionalrisk;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class InstitutionalRiskRulesTest {
  @Test void closureNeedsOwnerPlanDoneTreatmentsAndTamedResidual() {
    assertFalse(InstitutionalRiskRules.closable(null, "plan", true, 2, 3, null));       // no owner
    assertFalse(InstitutionalRiskRules.closable("owner", "plan", false, 2, 3, null));    // treatments open
    assertTrue(InstitutionalRiskRules.closable("owner", "plan", true, 2, 3, null));      // residual 6
    assertFalse(InstitutionalRiskRules.closable("owner", "plan", true, 4, 4, null));     // residual 16, unaccepted
    assertTrue(InstitutionalRiskRules.closable("owner", "plan", true, 4, 4, "المدير التنفيذي")); // formally accepted
    assertEquals(20, InstitutionalRiskRules.score(4, 5));
  }
}
