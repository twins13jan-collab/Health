package eg.gov.health.platform.criticalcare;

/** Unit-board ordering rules for the ICU operational board (Phase 4.6,
 *  cycle 221). The board is an attention queue, not a census list: a stay
 *  with an open escalation outranks everything; a stay whose device signal
 *  is lost outranks one with no data yet, which outranks stale data, which
 *  outranks a live stay. Signal state derives ONLY from the freshness of
 *  the newest device observation — stale data is labelled, never live. */
public final class IcuBoardRules {
  private IcuBoardRules() {}

  /** Signal state of a stay from its newest observation. */
  public static String signalOf(boolean hasObservation, IcuRules.Freshness newest) {
    if (!hasObservation || newest == null) return "no_data";
    return switch (newest) {
      case LIVE -> "live";
      case STALE -> "stale";
      case SIGNAL_LOST -> "signal_lost";
    };
  }

  /** Lower rank = needs attention first. */
  public static int attentionRank(boolean openEscalation, String signal) {
    if (openEscalation) return 0;
    return switch (signal == null ? "" : signal) {
      case "signal_lost" -> 1;
      case "no_data" -> 2;
      case "stale" -> 3;
      default -> 4; // live
    };
  }
}
