package eg.gov.health.platform.emergencytriage;
import java.time.*;
/** Emergency & triage rules (module 100). Door-to-doctor time
 *  is a promise scaled by acuity: ESI 1 immediate, 2 within
 *  10 minutes, 3 within 30, 4 within 60, 5 within 120 — and the
 *  clock is measured, not remembered. A patient who refuses
 *  service or leaves before assessment completes may do so —
 *  autonomy is real — but only after the risks are explained
 *  and the departure documented and signed; an undocumented
 *  walk-out is an unsafe departure, never a clean closure. */
public final class TriageRules {
  private TriageRules() {}
  public static long targetMinutes(int esi) {
    return switch (esi) { case 1 -> 0; case 2 -> 10; case 3 -> 30; case 4 -> 60; default -> 120; };
  }
  public static boolean doorToDoctorBreached(int esi, OffsetDateTime arrived, OffsetDateTime seen) {
    if (arrived == null || seen == null) return false;
    return Duration.between(arrived, seen).toMinutes() > targetMinutes(esi);
  }
  public static boolean departureDocumented(boolean risksExplained, boolean signed) {
    return risksExplained && signed;
  }
}
