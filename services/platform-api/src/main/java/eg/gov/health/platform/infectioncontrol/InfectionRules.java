package eg.gov.health.platform.infectioncontrol;
import java.math.*; import java.util.Set;
/** Infection prevention & control rules (module 91). Suspected and
 *  confirmed cases are defined and notifiable cases are reported —
 *  a confirmed notifiable case without notification is a breach, not
 *  a detail. Airborne precautions require a negative-pressure
 *  location. Only infection-control officers discontinue isolation.
 *  Contacts are traced within authority and followed to a result.
 *  HAI indicators are computed per 1,000 device-days from raw counts,
 *  never typed in. Antimicrobial stewardship ties continuation to
 *  culture results: when a narrower option exists, continuing the
 *  broad agent needs a documented stewardship decision. */
public final class InfectionRules {
  private InfectionRules() {}
  public static final Set<String> IPC_ROLES = Set.of("ipc_officer","infection_control","admin");
  public static boolean mayDiscontinueIsolation(String role) { return role != null && IPC_ROLES.contains(role); }
  public static boolean locationValid(String precautionType, String locationType) {
    if ("airborne".equals(precautionType)) return "negative_pressure".equals(locationType);
    return locationType != null && !locationType.isBlank();
  }
  public static boolean notificationBreach(boolean notifiable, String caseType, boolean notified) {
    return notifiable && "confirmed".equals(caseType) && !notified;
  }
  public static BigDecimal haiRatePer1000(int infections, int deviceDays) {
    if (deviceDays <= 0) return BigDecimal.ZERO;
    return new BigDecimal(infections).multiply(new BigDecimal(1000))
      .divide(new BigDecimal(deviceDays), 2, RoundingMode.HALF_UP);
  }
  public static boolean stewardshipDecisionRequired(String cultureResult, boolean narrowerOption, String decision) {
    // A culture-proven narrower option makes plain continuation a decision that must be owned.
    return cultureResult != null && narrowerOption && "continue".equals(decision);
  }
}
