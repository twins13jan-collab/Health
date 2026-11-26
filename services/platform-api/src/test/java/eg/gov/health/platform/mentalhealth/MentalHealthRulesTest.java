package eg.gov.health.platform.mentalhealth;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class MentalHealthRulesTest {
  @Test void treatingRolesOnly() {
    assertTrue(MentalHealthRules.canAccess("psychiatrist"));
    assertTrue(MentalHealthRules.canAccess("privacy_officer"));
    assertFalse(MentalHealthRules.canAccess("receptionist"));
    assertFalse(MentalHealthRules.canAccess("billing_clerk"));
    assertFalse(MentalHealthRules.canAccess(null));
  }
  @Test void phq9Bands() {
    assertEquals("minimal", MentalHealthRules.phq9Severity(3));
    assertEquals("mild", MentalHealthRules.phq9Severity(7));
    assertEquals("moderate", MentalHealthRules.phq9Severity(12));
    assertEquals("moderately_severe", MentalHealthRules.phq9Severity(18));
    assertEquals("severe", MentalHealthRules.phq9Severity(24));
    assertThrows(IllegalArgumentException.class, () -> MentalHealthRules.phq9Severity(28));
  }
  @Test void gad7Bands() {
    assertEquals("minimal", MentalHealthRules.gad7Severity(2));
    assertEquals("moderate", MentalHealthRules.gad7Severity(13));
    assertEquals("severe", MentalHealthRules.gad7Severity(19));
  }
  @Test void safetyPlanAndProtocolRules() {
    assertFalse(MentalHealthRules.safetyPlanRequired("moderate"));
    assertTrue(MentalHealthRules.safetyPlanRequired("high"));
    assertTrue(MentalHealthRules.safetyPlanRequired("imminent"));
    assertTrue(MentalHealthRules.activatesSafetyProtocol("imminent"));
    assertFalse(MentalHealthRules.activatesSafetyProtocol("high"));
  }
  @Test void ciwaBands() {
    assertEquals("mild", MentalHealthRules.ciwaBand(5));
    assertEquals("moderate", MentalHealthRules.ciwaBand(12));
    assertEquals("severe", MentalHealthRules.ciwaBand(18));
    assertEquals("very_severe", MentalHealthRules.ciwaBand(25));
    assertTrue(MentalHealthRules.ciwaNeedsMedicalMonitoring(18));
    assertFalse(MentalHealthRules.ciwaNeedsMedicalMonitoring(10));
  }
}
