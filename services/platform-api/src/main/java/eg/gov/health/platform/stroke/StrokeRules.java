package eg.gov.health.platform.stroke;
import java.time.*;
/** Stroke rules (module 88). The pathway runs on documented clocks:
 *  last-known-well, arrival, imaging, decision, needle. Thrombolysis
 *  is possible only inside the 4.5-hour window from last known well,
 *  only after CT has excluded hemorrhage, and only with NO absolute
 *  contraindication on the checklist; the door-to-needle target is
 *  60 minutes. A withheld decision is as documented as a given one.
 *  Urgent transfer to a capable centre requires documented acceptance
 *  before departure. No oral intake before a documented swallow
 *  screen. Discharge binds rehabilitation and secondary prevention. */
public final class StrokeRules {
  private StrokeRules() {}
  public static final long THROMBOLYSIS_WINDOW_MIN = 270; // 4.5 hours
  public static final long DOOR_TO_NEEDLE_TARGET_MIN = 60;
  public static boolean withinWindow(OffsetDateTime lastKnownWell, OffsetDateTime needle) {
    if (lastKnownWell == null || needle == null) return false;
    long m = Duration.between(lastKnownWell, needle).toMinutes();
    return m >= 0 && m <= THROMBOLYSIS_WINDOW_MIN;
  }
  public static boolean anyContraindication(boolean recentSurgery, boolean activeBleeding, boolean uncontrolledBp, boolean anticoagulantUse, boolean lowGlucoseMimic) {
    return recentSurgery || activeBleeding || uncontrolledBp || anticoagulantUse || lowGlucoseMimic;
  }
  public static boolean thrombolysisPermitted(boolean hemorrhageExcluded, boolean anyContraindication, OffsetDateTime lastKnownWell, OffsetDateTime needle) {
    return hemorrhageExcluded && !anyContraindication && withinWindow(lastKnownWell, needle);
  }
  public static boolean doorToNeedleBreached(OffsetDateTime arrival, OffsetDateTime needle) {
    if (arrival == null || needle == null) return false;
    return Duration.between(arrival, needle).toMinutes() > DOOR_TO_NEEDLE_TARGET_MIN;
  }
  public static boolean oralIntakePermitted(String swallowScreen) {
    return "passed".equals(swallowScreen);
  }
}
