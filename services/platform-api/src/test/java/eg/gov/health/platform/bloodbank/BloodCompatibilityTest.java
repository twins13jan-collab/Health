package eg.gov.health.platform.bloodbank;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class BloodCompatibilityTest {
  @Test void oNegativeDonatesToEveryone() {
    for (String g : BloodCompatibility.GROUPS) assertTrue(BloodCompatibility.canDonateRbc("O-", g), g);
  }
  @Test void abPositiveReceivesFromEveryone() {
    for (String g : BloodCompatibility.GROUPS) assertTrue(BloodCompatibility.canDonateRbc(g, "AB+"), g);
  }
  @Test void rhPositiveCannotDonateToRhNegative() {
    assertFalse(BloodCompatibility.canDonateRbc("A+", "A-"));
    assertTrue(BloodCompatibility.canDonateRbc("A-", "A+"));
  }
  @Test void aboRulesHold() {
    assertFalse(BloodCompatibility.canDonateRbc("A-", "B+"));
    assertTrue(BloodCompatibility.canDonateRbc("B-", "AB-"));
    assertFalse(BloodCompatibility.canDonateRbc("AB+", "O+"));
  }
  @Test void unknownGroupRejected() { assertFalse(BloodCompatibility.canDonateRbc("X-", "O-")); }
}
