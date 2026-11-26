package eg.gov.health.platform.workforcehealth;
import java.time.*;
/** Worker-health rules (module 56). Fitness for exposure duties
 *  is earned by a current periodic examination and current
 *  vaccinations; both expire, and an expired fitness is not a
 *  fitness. Every exposure — blood, infection, chemical,
 *  radiation, gas, biological — is investigated and followed up
 *  before its file closes, and PPE is issued with the training
 *  that makes it protection rather than costume. Psychological
 *  support records stay confidential behind role walls. */
public final class WorkerHealthRules {
  private WorkerHealthRules() {}
  public static final int EXAM_VALID_MONTHS = 12;
  public static boolean fitForExposureDuties(LocalDate lastExam, boolean vaccinationsCurrent, LocalDate today) {
    if (lastExam == null || today == null) return false;
    return vaccinationsCurrent && !lastExam.plusMonths(EXAM_VALID_MONTHS).isBefore(today);
  }
  public static boolean incidentClosable(boolean investigationDone, boolean followUpDone) {
    return investigationDone && followUpDone;
  }
  public static boolean ppeCompliant(boolean trainingDone) { return trainingDone; }
}
