package eg.gov.health.platform.protocolquality;
import java.time.*;
/** Protocol-quality rules (module 187). A protocol binds care
 *  only while it is CURRENT: withdrawn protocols apply to no
 *  one, and past its review date a protocol is stale until
 *  re-reviewed. Deviations are expected in real care — but each
 *  is either justified on the record or counted as unjustified,
 *  and an unjustified rate above 10% puts the protocol (or its
 *  training) under quality review. Alerts attach to the live
 *  version only. */
public final class ProtocolQualityRules {
  private ProtocolQualityRules() {}
  public static final double UNJUSTIFIED_REVIEW_ABOVE_PCT = 10.0;
  public static boolean applicable(String status, LocalDate reviewDue, LocalDate today) {
    return "current".equals(status) && (reviewDue == null || !reviewDue.isBefore(today));
  }
  public static double unjustifiedRatePct(long totalDeviations, long unjustified) {
    if (totalDeviations == 0) return 0.0;
    return Math.round(1000.0 * unjustified / totalDeviations) / 10.0;
  }
  public static double adherencePct(Integer total, Integer adherent) {
    if (total == null || total == 0 || adherent == null) return 0.0;
    return Math.round(1000.0 * adherent / total) / 10.0;
  }
}
