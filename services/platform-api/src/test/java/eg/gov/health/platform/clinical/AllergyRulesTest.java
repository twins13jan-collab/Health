package eg.gov.health.platform.clinical;
import org.junit.jupiter.api.Test; import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class AllergyRulesTest {
  AllergyRules.AllergyView pen() { return new AllergyRules.AllergyView("AMOXICILLIN","penicillins","severe","active"); }
  @Test void sameDrugConflicts() { assertTrue(AllergyRules.conflict("AMOXICILLIN", List.of(pen())).isPresent()); }
  @Test void sameClassConflicts() { assertTrue(AllergyRules.conflict("AMPICILLIN", List.of(pen())).isPresent()); }
  @Test void crossReactiveConflicts() { assertTrue(AllergyRules.conflict("CEFTRIAXONE", List.of(pen())).isPresent()); }
  @Test void unrelatedDrugClear() { assertTrue(AllergyRules.conflict("PARACETAMOL", List.of(pen())).isEmpty()); }
  @Test void resolvedAllergyIgnored() {
    var resolved = new AllergyRules.AllergyView("AMOXICILLIN","penicillins","severe","resolved");
    assertTrue(AllergyRules.conflict("AMOXICILLIN", List.of(resolved)).isEmpty());
  }
}
