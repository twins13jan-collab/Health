package eg.gov.health.platform.complexcare;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ComplexCareRulesTest {
  @Test void activationNeedsCoordinatorAndAResponsibleTeam() {
    assertTrue(ComplexCareRules.planActivatable("منسقة سارة", 2));
    assertFalse(ComplexCareRules.planActivatable(null, 3)); // a plan owned by everyone is owned by no one
    assertFalse(ComplexCareRules.planActivatable("منسقة سارة", 0));
  }
  @Test void transitionsNeedHandoverAndArrangedEquipment() {
    assertTrue(ComplexCareRules.transitionPermitted(true, true, true));
    assertFalse(ComplexCareRules.transitionPermitted(false, false, false)); // no handover
    assertFalse(ComplexCareRules.transitionPermitted(true, true, false));   // equipment needed, not arranged
    assertTrue(ComplexCareRules.transitionPermitted(true, false, false));   // no equipment needed
  }
  @Test void closureMeasuresQualityOfLife() {
    assertTrue(ComplexCareRules.closurePermitted(72.0, 0));
    assertFalse(ComplexCareRules.closurePermitted(null, 0)); // no silent closure
  }
}
