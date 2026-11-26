package eg.gov.health.platform.sensory;
import java.time.*; import java.util.Set;
/** Sensory rules (cycle 249). Every measurement binds an anatomical side,
 *  a device, a method and a unit — a number without a unit is never
 *  stored. A failed or stale calibration invalidates the result (marked,
 *  never presented as valid, retest required). Conflicting measurements
 *  keep the original and add a correction that supersedes it. An unfit
 *  assistive device is never delivered. */
public final class SensoryRules {
  private SensoryRules() {}
  public static final Set<String> SIDES = Set.of("left","right","bilateral");
  /** A complete measurement carries side + unit + device + method. */
  public static boolean measurementComplete(String side, String unit, String deviceCode, String method) {
    return side != null && SIDES.contains(side)
      && unit != null && !unit.isBlank()
      && deviceCode != null && !deviceCode.isBlank()
      && method != null && !method.isBlank();
  }
  /** Audiology validity: calibration passed and within 12 months. */
  public static boolean calibrationCurrent(boolean calibrationValid, LocalDate calibrationDate, LocalDate today) {
    return calibrationValid && calibrationDate != null && today != null
      && !calibrationDate.isBefore(today.minusMonths(12));
  }
  /** Plausible intraocular pressure (mmHg) is roughly 5-60; outside is suspect. */
  public static boolean iopSuspect(Double mmhg) { return mmhg != null && (mmhg < 5 || mmhg > 60); }
  /** Decimal visual acuity plausibility 0.0-2.0. */
  public static boolean acuityPlausible(Double v) { return v != null && v >= 0.0 && v <= 2.0; }
  /** Delivery requires a completed, successful fitting. */
  public static boolean deliverable(String fittingStatus) { return "fitted".equals(fittingStatus); }
}
