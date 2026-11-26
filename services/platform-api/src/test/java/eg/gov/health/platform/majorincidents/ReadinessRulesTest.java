package eg.gov.health.platform.majorincidents;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
class ReadinessRulesTest {
  @Test void activationNeedsChainCommsAndFreshExercise() {
    var today = LocalDate.parse("2026-11-22");
    assertTrue(ReadinessRules.activationReady("قائد -> نواب -> فرق", "راديو + رسائل ورقية", LocalDate.parse("2026-03-01"), today));
    assertFalse(ReadinessRules.activationReady(null, "راديو", LocalDate.parse("2026-03-01"), today));
    assertFalse(ReadinessRules.activationReady("سلسلة", " ", LocalDate.parse("2026-03-01"), today));
    assertFalse(ReadinessRules.activationReady("سلسلة", "راديو", LocalDate.parse("2024-01-01"), today)); // stale exercise
    assertFalse(ReadinessRules.activationReady("سلسلة", "راديو", null, today));
  }
  @Test void onlyVerifiedVoicesAndPeopleMove() {
    assertFalse(ReadinessRules.messagePublishable(false));
    assertTrue(ReadinessRules.messagePublishable(true));
    assertFalse(ReadinessRules.deploymentPermitted(false));
    assertTrue(ReadinessRules.deploymentPermitted(true));
  }
}
