package eg.gov.health.platform.wounds;
import java.util.Set;
/** Burns & wound rules (cycle 250). A comparable baseline and serial
 *  measurements define healing by an approved clinical definition —
 *  wound area change — never by impression. Suspected infection
 *  escalates regardless of measurements. Expired or recalled dressing
 *  material is blocked. Clinical images exist only with a documented
 *  consent basis and restricted viewing. Burn resuscitation follows
 *  Parkland: 4 ml x kg x %TBSA over 24h, half in the first 8 hours. */
public final class WoundRules {
  private WoundRules() {}
  public static double areaCm2(Double lengthCm, Double widthCm) {
    if (lengthCm == null || widthCm == null) return 0;
    return Math.round(lengthCm * widthCm * 100.0) / 100.0;
  }
  /** Percent area change vs baseline (negative = shrinking). */
  public static double areaChangePercent(double baselineArea, double currentArea) {
    if (baselineArea <= 0) return 0;
    return Math.round((currentArea - baselineArea) / baselineArea * 1000.0) / 10.0;
  }
  /** Healing verdict by the approved definition: area 0 = healed;
   *  reduction >=10% = improving; growth >10% = deteriorating. */
  public static String healingVerdict(double baselineArea, double currentArea, boolean infectionSuspected) {
    if (infectionSuspected) return "infection_suspected_escalate";
    if (currentArea <= 0) return "healed";
    double change = areaChangePercent(baselineArea, currentArea);
    if (change <= -10.0) return "improving";
    if (change >= 10.0) return "deteriorating_escalate";
    return "static_continue_plan";
  }
  public static double parklandMl24h(double weightKg, double tbsaPercent) { return 4.0 * weightKg * tbsaPercent; }
  public static double parklandFirst8h(double weightKg, double tbsaPercent) { return parklandMl24h(weightKg, tbsaPercent) / 2.0; }
  public static boolean tbsaValid(Double tbsa) { return tbsa != null && tbsa >= 0 && tbsa <= 100; }
  public static final Set<String> CONSENT_BASES = Set.of("patient_consent","legal_guardian_consent","clinical_necessity_documented");
  public static boolean imageConsentValid(String basis) { return basis != null && CONSENT_BASES.contains(basis); }
  /** Roles allowed to view restricted clinical images. */
  public static final Set<String> IMAGE_VIEW_ROLES = Set.of("wound_specialist","physician","privacy_officer","admin");
  public static boolean mayViewImage(String role) { return role != null && IMAGE_VIEW_ROLES.contains(role); }
  public static boolean materialUsable(String lotStatus) { return "ok".equals(lotStatus); }
}
