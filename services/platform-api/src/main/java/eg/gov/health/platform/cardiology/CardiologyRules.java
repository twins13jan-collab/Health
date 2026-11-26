package eg.gov.health.platform.cardiology;
import java.time.*;
/** Cardiology & cath rules (module 87). Chest-pain and MI pathways run
 *  on the clock: for STEMI the door-to-balloon target is 90 minutes
 *  and every breach is flagged, never hidden. A cath procedure cannot
 *  start until the preparation checklist is complete — informed
 *  consent, allergy check, renal function recorded, antiplatelet
 *  loading and a confirmed team. High-value supplies (stents,
 *  implanted devices) are traceable by serial/lot and linked to the
 *  procedure, the cost and the claim. Contrast load against impaired
 *  renal function raises an AKI watch. */
public final class CardiologyRules {
  private CardiologyRules() {}
  public static final long DOOR_TO_BALLOON_TARGET_MIN = 90;
  public static long doorToBalloonMinutes(OffsetDateTime arrival, OffsetDateTime balloon) {
    if (arrival == null || balloon == null) return -1;
    return Duration.between(arrival, balloon).toMinutes();
  }
  public static boolean doorToBalloonBreached(OffsetDateTime arrival, OffsetDateTime balloon) {
    long m = doorToBalloonMinutes(arrival, balloon);
    return m >= 0 && m > DOOR_TO_BALLOON_TARGET_MIN;
  }
  public static boolean preparationComplete(boolean consent, boolean allergyChecked, String renalFunction, boolean antiplateletLoaded, boolean teamConfirmed) {
    return consent && allergyChecked && renalFunction != null && !renalFunction.isBlank() && antiplateletLoaded && teamConfirmed;
  }
  public static boolean implantTraceable(String serialLot) {
    return serialLot != null && !serialLot.isBlank();
  }
  public static boolean akiWatch(Integer contrastMl, String renalFunction) {
    return contrastMl != null && contrastMl >= 200 && renalFunction != null && renalFunction.toLowerCase().contains("impaired");
  }
}
