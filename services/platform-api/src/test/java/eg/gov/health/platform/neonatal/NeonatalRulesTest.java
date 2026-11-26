package eg.gov.health.platform.neonatal;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class NeonatalRulesTest {
  @Test void apgarBands() {
    assertEquals("reassuring", NeonatalRules.apgarBand(9));
    assertEquals("moderately_abnormal", NeonatalRules.apgarBand(5));
    assertEquals("low_critical", NeonatalRules.apgarBand(2));
    assertTrue(NeonatalRules.apgarNeedsEscalation(2));
    assertThrows(IllegalArgumentException.class, () -> NeonatalRules.apgarBand(11));
  }
  @Test void birthWeightClasses() {
    assertEquals("elbw", NeonatalRules.birthWeightClass(900));
    assertEquals("vlbw", NeonatalRules.birthWeightClass(1400));
    assertEquals("lbw", NeonatalRules.birthWeightClass(2400));
    assertEquals("normal", NeonatalRules.birthWeightClass(3200));
    assertEquals("macrosomia", NeonatalRules.birthWeightClass(4200));
    assertTrue(NeonatalRules.isLowBirthWeight(2400));
  }
  @Test void weightLossAlert() {
    assertEquals(5.0, NeonatalRules.weightLossPct(3000, 2850));
    assertFalse(NeonatalRules.excessiveWeightLoss(3000, 2850));
    assertTrue(NeonatalRules.excessiveWeightLoss(3000, 2650));
  }
  @Test void screeningOutcomeRules() {
    var invalid = NeonatalRules.onResult("invalid");
    assertTrue(invalid.recollectRequired()); assertFalse(invalid.referralOpens());
    var abnormal = NeonatalRules.onResult("abnormal");
    assertTrue(abnormal.referralOpens()); assertTrue(abnormal.requiresAcknowledgment());
    var normal = NeonatalRules.onResult("normal");
    assertFalse(normal.recollectRequired()); assertFalse(normal.referralOpens());
  }
  @Test void referralClosureNeedsOutcome() {
    assertTrue(NeonatalRules.referralClosable("open", "referred to audiology, plan set"));
    assertFalse(NeonatalRules.referralClosable("open", " "));
    assertFalse(NeonatalRules.referralClosable("none", "done"));
  }
  @Test void twinsNeedDoubleVerification() {
    assertTrue(NeonatalRules.doubleVerificationRequired(2));
    assertFalse(NeonatalRules.doubleVerificationRequired(1));
  }
}
