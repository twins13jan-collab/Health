package eg.gov.health.platform.clinical;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class News2CalculatorTest {
  @Test void normalVitalsScoreZero() {
    var s = News2Calculator.score(new News2Calculator.Vitals(16, 98, false, 120, 80, 37.0, "alert"));
    assertEquals(0, s.total()); assertEquals("low", s.riskBand());
  }
  @Test void deterioratingPatientScoresHigh() {
    var s = News2Calculator.score(new News2Calculator.Vitals(28, 90, true, 95, 125, 39.5, "voice"));
    assertTrue(s.total() >= 7, "expected >=7 got " + s.total()); assertEquals("high", s.riskBand());
  }
  @Test void singleParameterThreeIsLowMedium() {
    var s = News2Calculator.score(new News2Calculator.Vitals(16, 98, false, 120, 80, 34.5, "alert"));
    assertEquals(3, s.total()); assertEquals("low-medium", s.riskBand());
  }
  @Test void oxygenAddsTwo() {
    var a = News2Calculator.score(new News2Calculator.Vitals(16, 97, false, 120, 80, 37.0, "alert"));
    var b = News2Calculator.score(new News2Calculator.Vitals(16, 97, true, 120, 80, 37.0, "alert"));
    assertEquals(a.total() + 2, b.total());
  }
}
