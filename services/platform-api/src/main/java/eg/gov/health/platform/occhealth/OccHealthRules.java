package eg.gov.health.platform.occhealth;
import java.time.*; import java.util.*;
/** Occupational-health rules for high-risk sectors (module 144).
 *  Surveillance is scheduled: a worker whose periodic examination
 *  is overdue is NOT cleared for high-risk duty, however experienced.
 *  An exposure incident (radiation, noise, heat, chemical,
 *  biological) requires a post-exposure examination before return
 *  to the exposure duty. And the employer's report observes
 *  disclosure limits: the employer receives the fitness status and
 *  work restrictions — never the diagnosis. */
public final class OccHealthRules {
  private OccHealthRules() {}
  public static boolean examOverdue(LocalDate nextDueAt, LocalDate today) {
    return nextDueAt != null && today != null && today.isAfter(nextDueAt);
  }
  public static boolean clearedForDuty(String latestResult, boolean overdue, boolean openExposureWithoutFollowUp) {
    return !overdue && !openExposureWithoutFollowUp
      && ("fit".equals(latestResult) || "fit_with_restrictions".equals(latestResult));
  }
  /** The ONLY payload an employer may receive: fitness + restrictions. Diagnosis is never in it. */
  public static Map<String,Object> employerDisclosure(String result, String restrictions) {
    var m = new LinkedHashMap<String,Object>();
    m.put("fitness_status", result);
    m.put("work_restrictions", restrictions);
    m.put("disclosure_scope", "fitness status and restrictions only — diagnosis is never disclosed to the employer");
    return m;
  }
}
