package eg.gov.health.platform.clinical;
import java.util.*;
/** WHO Surgical Safety Checklist gating (cycle 220): a case cannot move to
 *  the next stage until the current stage's items are ALL confirmed. The
 *  stages are sign_in -> time_out -> sign_out, and skipping is impossible
 *  by construction: completion of a stage is the only key to the next. */
public final class WhoChecklist {
  private WhoChecklist() {}
  public static final Map<String, List<String>> STAGES = Map.of(
    "sign_in", List.of("identity_confirmed","site_marked","consent_confirmed","anaesthesia_check","allergy_checked","airway_assessed"),
    "time_out", List.of("team_introduced","patient_site_procedure_confirmed","antibiotic_prophylaxis","imaging_displayed","anticipated_events_reviewed"),
    "sign_out", List.of("procedure_recorded","counts_correct","specimen_labelled","equipment_issues_noted","recovery_plan_reviewed"));
  public static final List<String> STAGE_ORDER = List.of("sign_in","time_out","sign_out");
  public static boolean stageComplete(String stage, Set<String> confirmedItems) {
    var required = STAGES.get(stage);
    return required != null && confirmedItems.containsAll(required);
  }
  public static Optional<String> nextStage(String currentStage, Set<String> confirmedItems) {
    int idx = STAGE_ORDER.indexOf(currentStage);
    if (idx < 0) return Optional.empty();
    if (!stageComplete(currentStage, confirmedItems)) return Optional.empty();
    return idx + 1 < STAGE_ORDER.size() ? Optional.of(STAGE_ORDER.get(idx + 1)) : Optional.of("completed");
  }
}
