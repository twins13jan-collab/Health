package eg.gov.health.platform.majorincidents;
import java.time.*;
/** Readiness rules (module 76). A major-incident plan is only
 *  real if it has been exercised: readiness decays, and a plan
 *  whose last exercise is older than twelve months — or that
 *  lacks a command chain or an alternative communications path
 *  for when the primary one fails — cannot be activated as
 *  "ready". Public messages publish only from verified sources;
 *  in a crisis, an unverified message is how rumours get an
 *  official voice. Volunteers deploy only when verified. */
public final class ReadinessRules {
  private ReadinessRules() {}
  public static final int EXERCISE_VALID_MONTHS = 12;
  public static boolean activationReady(String commandChain, String alternativeComms, LocalDate lastExercise, LocalDate today) {
    if (commandChain == null || commandChain.isBlank()) return false;
    if (alternativeComms == null || alternativeComms.isBlank()) return false;
    if (lastExercise == null || today == null) return false;
    return !lastExercise.plusMonths(EXERCISE_VALID_MONTHS).isBefore(today);
  }
  public static boolean messagePublishable(boolean sourceVerified) { return sourceVerified; }
  public static boolean deploymentPermitted(boolean verified) { return verified; }
}
