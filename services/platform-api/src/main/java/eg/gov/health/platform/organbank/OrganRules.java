package eg.gov.health.platform.organbank;
import java.math.*; import java.time.*; import java.util.Map;
/** Organ & tissue rules (module 129). Donation, evaluation, consent
 *  and allocation follow the law: no allocation without documented
 *  consent, a completed medical evaluation, a clean coercion screen
 *  and committee approval. Any coercion or unethical-incentive flag
 *  blocks the case outright — trafficking prevention is a hard
 *  stop, not a warning. The cold chain is timed: ischemia limits
 *  per graft type are computed and breaches flagged, and transport
 *  temperature excursions are flagged against the graft's range.
 *  Every graft is traceable from source to recipient. */
public final class OrganRules {
  private OrganRules() {}
  public static final Map<String,Integer> ISCHEMIA_LIMIT_H = Map.of(
    "heart", 6, "lung", 6, "liver", 12, "kidney", 24, "pancreas", 18, "cornea", 336);
  public static boolean allocationPermitted(boolean consentDocumented, boolean evaluationComplete, boolean coercionScreened,
                                            boolean incentiveFlag, boolean committeeApproved) {
    return consentDocumented && evaluationComplete && coercionScreened && !incentiveFlag && committeeApproved;
  }
  public static boolean caseBlocked(boolean incentiveFlag, boolean coercionScreened) {
    return incentiveFlag || !coercionScreened;
  }
  public static long ischemiaMinutes(OffsetDateTime start, OffsetDateTime end) {
    if (start == null || end == null) return -1;
    return Duration.between(start, end).toMinutes();
  }
  public static boolean ischemiaBreached(String organ, OffsetDateTime start, OffsetDateTime end) {
    long m = ischemiaMinutes(start, end);
    if (m < 0) return false;
    int limit = ISCHEMIA_LIMIT_H.getOrDefault(organ == null ? "" : organ.toLowerCase(), 24);
    return m > limit * 60L;
  }
  public static boolean temperatureExcursion(String organ, BigDecimal tempC) {
    if (tempC == null) return false;
    // Preservation range for solid organs/tissues: 2–8 °C.
    return tempC.compareTo(new BigDecimal("2")) < 0 || tempC.compareTo(new BigDecimal("8")) > 0;
  }
}
