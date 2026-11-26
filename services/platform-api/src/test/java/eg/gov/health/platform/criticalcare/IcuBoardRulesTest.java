package eg.gov.health.platform.criticalcare;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class IcuBoardRulesTest {

  @Test void signalFollowsNewestObservationFreshness() {
    assertEquals("live", IcuBoardRules.signalOf(true, IcuRules.Freshness.LIVE));
    assertEquals("stale", IcuBoardRules.signalOf(true, IcuRules.Freshness.STALE));
    assertEquals("signal_lost", IcuBoardRules.signalOf(true, IcuRules.Freshness.SIGNAL_LOST));
  }

  @Test void noObservationIsNoDataNeverLive() {
    assertEquals("no_data", IcuBoardRules.signalOf(false, null));
    assertEquals("no_data", IcuBoardRules.signalOf(false, IcuRules.Freshness.LIVE));
  }

  @Test void openEscalationOutranksEverything() {
    assertEquals(0, IcuBoardRules.attentionRank(true, "live"));
    assertEquals(0, IcuBoardRules.attentionRank(true, "signal_lost"));
    assertTrue(IcuBoardRules.attentionRank(true, "live") < IcuBoardRules.attentionRank(false, "signal_lost"));
  }

  @Test void signalLostOutranksNoDataStaleAndLive() {
    assertTrue(IcuBoardRules.attentionRank(false, "signal_lost") < IcuBoardRules.attentionRank(false, "no_data"));
    assertTrue(IcuBoardRules.attentionRank(false, "no_data") < IcuBoardRules.attentionRank(false, "stale"));
    assertTrue(IcuBoardRules.attentionRank(false, "stale") < IcuBoardRules.attentionRank(false, "live"));
  }

  @Test void unknownSignalSortsAsLive() {
    assertEquals(4, IcuBoardRules.attentionRank(false, "live"));
    assertEquals(4, IcuBoardRules.attentionRank(false, null));
    assertEquals(4, IcuBoardRules.attentionRank(false, "anything-else"));
  }
}
