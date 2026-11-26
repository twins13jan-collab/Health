package eg.gov.health.platform.labsafety;
import java.time.*;
/** Laboratory-safety rules (module 158). A new test enters
 *  service only after its pre-introduction risk assessment and
 *  the verification of accuracy, sensitivity and interferences —
 *  a test that was never verified produces numbers nobody should
 *  act on. Reagent lots are used within their validity: an
 *  expired lot is not a reagent, it is a hazard. And when an
 *  analyzer fails, the question is never only "is it fixed" —
 *  the impact on results already reported must be assessed
 *  before the analyzer returns to service. */
public final class LabSafetyRules {
  private LabSafetyRules() {}
  public static boolean introductionApprovable(boolean risk, boolean accuracy, boolean sensitivity, boolean interferences) {
    return risk && accuracy && sensitivity && interferences;
  }
  public static boolean lotUsable(LocalDate expiryDate, LocalDate today) {
    return expiryDate != null && today != null && !expiryDate.isBefore(today);
  }
  public static boolean returnToServicePermitted(boolean impactAssessed) { return impactAssessed; }
  public static boolean comparisonAcceptable(Double biasPct) { return biasPct != null && Math.abs(biasPct) <= 10.0; }
}
