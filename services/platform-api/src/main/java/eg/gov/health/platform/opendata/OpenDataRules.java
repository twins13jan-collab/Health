package eg.gov.health.platform.opendata;
import java.util.*;
/** Open-data rules (module 188). A dataset is published only
 *  when its re-identification risk has been assessed, it is
 *  anonymized with small cells suppressed (minimum cell 10 —
 *  below that, a "statistic" can be a named person), and its
 *  definitions, methodology and source are documented. And a
 *  published dataset found wrong or privacy-risky is withdrawn
 *  immediately, with the reason on record: open data that
 *  cannot be withdrawn is a leak with a portal. */
public final class OpenDataRules {
  private OpenDataRules() {}
  public static final int MIN_CELL = 10;
  public static List<String> publishBlockers(boolean riskAssessed, boolean anonymized, Integer minCellSize, boolean definitionsDocumented) {
    List<String> b = new ArrayList<>();
    if (!riskAssessed) b.add("reidentification_risk_unassessed");
    if (!anonymized) b.add("not_anonymized");
    if (minCellSize == null || minCellSize < MIN_CELL) b.add("small_cells_unsuppressed");
    if (!definitionsDocumented) b.add("definitions_undocumented");
    return b;
  }
}
