package eg.gov.health.platform.programs;
import org.junit.jupiter.api.Test; import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
class ChemoDosingTest {
  @Test void bsaMostellerKnownValue() {
    // 170cm, 70kg -> sqrt(170*70/3600) = sqrt(3.3056) = 1.8181...
    assertEquals(1.818, ChemoDosing.bsaMosteller(170, 70), 0.001);
  }
  @Test void fullDoseAtNormalRenalFunction() {
    var dose = ChemoDosing.adjustedDoseMg(1.818, new BigDecimal("75"), 90);
    assertEquals(new BigDecimal("136.4"), dose);
  }
  @Test void renalBands() {
    assertEquals(new BigDecimal("75"), ChemoDosing.renalAdjustmentPct(50));
    assertEquals(new BigDecimal("50"), ChemoDosing.renalAdjustmentPct(30));
    assertEquals(BigDecimal.ZERO, ChemoDosing.renalAdjustmentPct(15));
  }
  @Test void invalidAnthropometricsRejected() { assertThrows(IllegalArgumentException.class, () -> ChemoDosing.bsaMosteller(0, 70)); }
}
