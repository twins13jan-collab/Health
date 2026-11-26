package eg.gov.health.platform.clinicalriskmatrix;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class MatrixRulesTest {
  @Test void controlNeedsAllThreeLayers() {
    assertTrue(MatrixRules.fullyControlled("منع", "كشف", "استجابة"));
    assertFalse(MatrixRules.fullyControlled("منع", "", "استجابة"));
    assertFalse(MatrixRules.fullyControlled("منع", "كشف", null));
    assertEquals("extreme", MatrixRules.band(20));
    assertEquals("high", MatrixRules.band(10));
    assertEquals("low", MatrixRules.band(2));
  }
  @Test void alertFatigueNeedsRateAndSample() {
    assertTrue(MatrixRules.alertFatigued(100, 62));
    assertFalse(MatrixRules.alertFatigued(100, 40));
    assertFalse(MatrixRules.alertFatigued(10, 9));   // too small a sample to judge
  }
}
