package eg.gov.health.platform.healthequity;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class EquityRulesTest {
  @Test void distributionIsMeasuredAgainstNeedNotJustHeads() {
    // 20 units / 100k people = 2.0 per 10k raw; burden 2.5 -> 0.8 adjusted -> underserved
    assertEquals(0.8, EquityRules.needAdjustedUnitsPer10k(20, 100000, 2.5));
    assertTrue(EquityRules.underserved(0.8));
    assertEquals(2.0, EquityRules.needAdjustedUnitsPer10k(20, 100000, 1.0));
    assertFalse(EquityRules.underserved(2.0));
  }
  @Test void smallSamplesAreSuppressedAndProgramsCloseOnEvidence() {
    assertFalse(EquityRules.gapPublishable(12));
    assertTrue(EquityRules.gapPublishable(30));
    assertFalse(EquityRules.programClosable(18.0, 10.0));
    assertTrue(EquityRules.programClosable(9.5, 10.0));
    assertFalse(EquityRules.programClosable(null, 10.0));
  }
}
