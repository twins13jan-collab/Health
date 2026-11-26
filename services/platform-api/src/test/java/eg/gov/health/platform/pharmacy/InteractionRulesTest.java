package eg.gov.health.platform.pharmacy;
import org.junit.jupiter.api.Test; import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class InteractionRulesTest {
  @Test void warfarinAspirinIsSevere() {
    assertTrue(InteractionRules.severeInteraction("WARFARIN", List.of("ASPIRIN")).isPresent());
    assertTrue(InteractionRules.severeInteraction("aspirin", List.of("warfarin")).isPresent());
  }
  @Test void unrelatedPairIsFine() { assertTrue(InteractionRules.severeInteraction("PARACETAMOL", List.of("AMOXICILLIN")).isEmpty()); }
  @Test void duplicateDetection() {
    assertTrue(InteractionRules.isDuplicate("INSULIN", List.of("insulin")));
    assertFalse(InteractionRules.isDuplicate("INSULIN", List.of("METFORMIN")));
  }
}
