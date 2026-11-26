package eg.gov.health.platform.proms;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class PromRulesTest {
  @Test void onlyValidatedInstrumentsAreScheduled() {
    assertTrue(PromRules.instrumentUsable(true));
    assertFalse(PromRules.instrumentUsable(false));
  }
  @Test void urgentSignalsBlockClosureUntilFollowedUp() {
    assertFalse(PromRules.scheduleClosable(true, false));
    assertTrue(PromRules.scheduleClosable(true, true));
    assertTrue(PromRules.scheduleClosable(false, false));
  }
  @Test void smallSamplesDescribeRespondentsNotTheService() {
    assertFalse(PromRules.analysisPublishable(12, 40.0));
    assertTrue(PromRules.analysisPublishable(120, 40.0));
    assertFalse(PromRules.analysisPublishable(120, null));
  }
}
