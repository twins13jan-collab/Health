package eg.gov.health.platform.criticalcare;
import java.time.*;
/** ICU & device-monitoring rules (cycle 221). Catalog invariants encoded:
 *  device data that is not fresh must never be presented as live; a weight
 *  or unit discrepancy blocks automatic dose calculation until the reference
 *  weight is verified; critical alarms may be acknowledged but never
 *  permanently silenced; automated predictions never become treatment
 *  orders without clinician approval (enforced in the controller flows). */
public final class IcuRules {
  private IcuRules() {}
  public enum Freshness { LIVE, STALE, SIGNAL_LOST }
  /** <=120s live, <=600s stale (shown as not-current), beyond: signal lost. */
  public static Freshness freshness(OffsetDateTime sourceTime, OffsetDateTime now) {
    long age = Duration.between(sourceTime, now).getSeconds();
    if (age <= 120) return Freshness.LIVE;
    if (age <= 600) return Freshness.STALE;
    return Freshness.SIGNAL_LOST;
  }
  public static boolean mayPresentAsLive(Freshness f) { return f == Freshness.LIVE; }
  /** Physiological plausibility ranges per observation kind. */
  public static boolean isPhysiologic(String kind, double v) {
    return switch (kind) {
      case "heart_rate" -> v >= 20 && v <= 250;
      case "spo2" -> v >= 50 && v <= 100;
      case "respiratory_rate" -> v >= 4 && v <= 60;
      case "temperature" -> v >= 30 && v <= 43;
      case "map" -> v >= 20 && v <= 200;
      case "systolic_bp" -> v >= 40 && v <= 300;
      case "gcs" -> v >= 3 && v <= 15;
      case "urine_output" -> v >= 0 && v <= 1000;
      default -> true;
    };
  }
  public static String qualityFor(String kind, double v) { return isPhysiologic(kind, v) ? "good" : "suspect"; }
  /** Normalises a recorded weight to kg (1 lb = 0.45359237 kg, 1 decimal). */
  public static double normalizedWeightKg(double weight, String unit) {
    if (weight <= 0) throw new IllegalArgumentException("weight must be positive");
    if ("kg".equals(unit)) return weight;
    if ("lb".equals(unit)) return Math.round(weight * 0.45359237 * 10.0) / 10.0;
    throw new IllegalArgumentException("unknown weight unit: " + unit);
  }
  /** Automatic (weight-based) dose calculation is allowed only against a
   *  VERIFIED reference weight — any weight/unit discrepancy blocks it. */
  public static boolean doseCalculationAllowed(boolean weightVerified) { return weightVerified; }
  /** Weight-based total rate: ratePerKg x verified kg. */
  public static double totalRate(double ratePerKg, double weightKg) {
    if (ratePerKg < 0 || weightKg <= 0) throw new IllegalArgumentException("invalid rate/weight");
    return Math.round(ratePerKg * weightKg * 100.0) / 100.0;
  }
  /** Critical-alarm silence: at most 120 seconds, never permanent. */
  public static final int MAX_SILENCE_SECONDS = 120;
  public static boolean silenceAllowed(int seconds) { return seconds > 0 && seconds <= MAX_SILENCE_SECONDS; }
}
