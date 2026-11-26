package eg.gov.health.platform.journey;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
class JourneyRulesTest {
  @Test void canonicalOrderNoBackwardsAndSpine() {
    assertTrue(JourneyRules.canRecord(Set.of(), "registration"));
    assertTrue(JourneyRules.canRecord(Set.of("encounter","diagnostics"), "pharmacy"));
    assertTrue(JourneyRules.canRecord(Set.of("encounter","diagnostics"), "diagnostics"));
    assertFalse(JourneyRules.canRecord(Set.of("encounter","billing"), "triage"));
    assertFalse(JourneyRules.canRecord(Set.of("encounter"), "teleport"));
    assertTrue(JourneyRules.missingMandatory(Set.of("encounter","diagnostics","pharmacy","billing","discharge")).isEmpty());
    assertTrue(JourneyRules.missingMandatory(Set.of("encounter","treatment","pharmacy","billing","discharge")).isEmpty());
    assertTrue(JourneyRules.missingMandatory(Set.of("encounter")).contains("billing"));
    assertTrue(JourneyRules.missingMandatory(Set.of("encounter")).contains("diagnostics_or_treatment"));
  }
}
