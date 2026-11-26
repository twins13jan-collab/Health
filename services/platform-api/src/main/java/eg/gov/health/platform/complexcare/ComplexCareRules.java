package eg.gov.health.platform.complexcare;
/** Complex-case rules (module 142). A multi-party care plan
 *  activates only with a NAMED care coordinator and at least one
 *  team member carrying a defined responsibility — a plan owned by
 *  everyone is owned by no one. A transition between hospital,
 *  home and rehabilitation needs a documented handover, and when
 *  home equipment is needed it must be arranged before the patient
 *  moves. Continuity and quality of life are measured at closure:
 *  a plan does not close silently. */
public final class ComplexCareRules {
  private ComplexCareRules() {}
  public static boolean planActivatable(String coordinator, long teamMembersWithResponsibility) {
    return coordinator != null && !coordinator.isBlank() && teamMembersWithResponsibility >= 1;
  }
  public static boolean transitionPermitted(boolean handoverDocumented, boolean equipmentNeeded, boolean equipmentArranged) {
    return handoverDocumented && (!equipmentNeeded || equipmentArranged);
  }
  public static boolean closurePermitted(Double qualityOfLifeScore, long openTransitions) {
    return qualityOfLifeScore != null && openTransitions == 0;
  }
}
