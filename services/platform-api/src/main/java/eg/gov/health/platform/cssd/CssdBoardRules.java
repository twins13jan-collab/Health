package eg.gov.health.platform.cssd;
import java.time.*; import java.util.*;
/** CSSD board rules (module 105, Phase 4.10). Sterile storage is a
 *  promise with a date on it: a set whose expiry passed while still
 *  marked sterile is a hazard, not stock — it tops the board with
 *  recalls and quarantines. Sets expiring within EXPIRY_WARN_DAYS
 *  are flagged so they are used or reprocessed in time. Sterilizers
 *  carry maintenance and calibration due dates; an overdue
 *  sterilizer is department-level attention, because every load it
 *  ran is suspect. The reprocessing chain names its missing steps —
 *  a set is never just "not ready". */
public final class CssdBoardRules {
  private CssdBoardRules() {}
  public static final long EXPIRY_WARN_DAYS = 7;
  public static final long STERILIZER_DUE_WARN_DAYS = 30;
  public static String expiryState(String setStatus, LocalDate expiry, LocalDate today) {
    if (!"sterile".equals(setStatus) || expiry == null || today == null) return "na";
    if (today.isAfter(expiry)) return "expired";
    if (!expiry.isAfter(today.plusDays(EXPIRY_WARN_DAYS))) return "expiring_soon";
    return "valid";
  }
  public static String dueState(LocalDate due, LocalDate today) {
    if (due == null || today == null) return "unknown";
    if (today.isAfter(due)) return "overdue";
    if (!due.isAfter(today.plusDays(STERILIZER_DUE_WARN_DAYS))) return "due_soon";
    return "ok";
  }
  public static List<String> missingSteps(boolean cleaning, boolean disinfection, boolean dried, boolean inspected, String packedBy) {
    var m = new ArrayList<String>();
    if (!cleaning) m.add("cleaning");
    if (!disinfection) m.add("disinfection");
    if (!dried) m.add("drying");
    if (!inspected) m.add("inspection");
    if (packedBy == null || packedBy.isBlank()) m.add("packing");
    return m;
  }
  /** Recalled first, then quarantined, expired stock, expiring stock,
   *  work in process, then everything else. */
  public static int setAttentionRank(String setStatus, String expiryState) {
    if ("recalled".equals(setStatus)) return 0;
    if ("quarantined".equals(setStatus)) return 1;
    if ("expired".equals(expiryState)) return 2;
    if ("expiring_soon".equals(expiryState)) return 3;
    if ("in_process".equals(setStatus)) return 4;
    return 5;
  }
}
