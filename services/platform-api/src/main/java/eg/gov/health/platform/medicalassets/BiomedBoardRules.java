package eg.gov.health.platform.medicalassets;
import java.time.LocalDate;
/** Attention rules for the biomedical-engineering board (cycle
 *  230 operations layer). A device that hurt or could have hurt a
 *  patient tops the queue; then devices out of service; then
 *  out-of-calibration devices (unfit by AssetRules even when
 *  switched on); quarantined; under maintenance; calibration
 *  falling due; preventive maintenance overdue. Due-soon windows:
 *  calibration 30 days, preventive maintenance 14 days. */
public final class BiomedBoardRules {
  private BiomedBoardRules() {}
  public static final int CALIBRATION_WARN_DAYS = 30;
  public static final int MAINTENANCE_WARN_DAYS = 14;

  public static String calibrationState(LocalDate due, LocalDate today) {
    if (due == null) return "unscheduled";
    if (today.isAfter(due)) return "overdue";
    if (!due.isAfter(today.plusDays(CALIBRATION_WARN_DAYS))) return "due_soon";
    return "current";
  }
  public static String maintenanceState(LocalDate nextDue, LocalDate today) {
    if (nextDue == null) return "unscheduled";
    if (today.isAfter(nextDue)) return "overdue";
    if (!nextDue.isAfter(today.plusDays(MAINTENANCE_WARN_DAYS))) return "due_soon";
    return "current";
  }
  /** 0 = act now … 9 = no attention needed (off the queue). */
  public static int assetRank(String status, String calibrationState, String maintenanceState,
                              boolean patientIncidentOpen) {
    if (patientIncidentOpen) return 0;
    if ("out_of_service".equals(status)) return 1;
    if ("overdue".equals(calibrationState)) return 2;
    if ("quarantined".equals(status)) return 3;
    if ("under_maintenance".equals(status)) return 4;
    if ("due_soon".equals(calibrationState)) return 5;
    if ("overdue".equals(maintenanceState)) return 6;
    if ("due_soon".equals(maintenanceState)) return 7;
    return 9;
  }
}
