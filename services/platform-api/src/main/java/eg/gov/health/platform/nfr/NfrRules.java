package eg.gov.health.platform.nfr;
import java.time.*;
/** NFR rules (module 176). A non-functional requirement is met
 *  only by evidence: a measurement within its target, no older
 *  than 30 days. An unmeasured requirement is UNVERIFIED — not
 *  met, not failed, unknown — and a service carrying an unmet
 *  or unverified CRITICAL requirement (availability, security)
 *  is not ready, whatever the feature checklist says. */
public final class NfrRules {
  private NfrRules() {}
  public static final int EVIDENCE_MAX_AGE_DAYS = 30;
  public static boolean withinTarget(Double measured, Double target, String comparison) {
    if (measured == null || target == null) return false;
    return "at_least".equals(comparison) ? measured >= target : measured <= target;
  }
  public static String status(Double measured, Double target, String comparison, LocalDate measuredOn, LocalDate today) {
    if (measuredOn == null || measured == null) return "unverified";
    if (measuredOn.plusDays(EVIDENCE_MAX_AGE_DAYS).isBefore(today)) return "unverified";
    return withinTarget(measured, target, comparison) ? "met" : "unmet";
  }
}
