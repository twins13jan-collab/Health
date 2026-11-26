package eg.gov.health.platform.maternity;
import java.time.*; import java.time.temporal.ChronoUnit;
/** Maternity rules (cycle 244). Risk scoring is ADVISORY: it proposes, the
 *  obstetrician disposes. Hard rules: postpartum haemorrhage thresholds by
 *  delivery type trigger the emergency path; a signed-off delivery record
 *  is immutable; discharge is blocked while danger signs persist. */
public final class MaternityRules {
  private MaternityRules() {}
  /** Naegele: EDD = LMP + 280 days. */
  public static LocalDate edd(LocalDate lmp) { if (lmp==null) return null; return lmp.plusDays(280); }
  public record GestationalAge(int weeks, int days) {}
  public static GestationalAge gestationalAge(LocalDate lmp, LocalDate on) {
    if (lmp==null||on==null||on.isBefore(lmp)) throw new IllegalArgumentException("invalid dates for gestational age");
    long d = ChronoUnit.DAYS.between(lmp, on); return new GestationalAge((int)(d/7), (int)(d%7));
  }
  public static boolean severePreeclampsia(Integer sys, Integer dia) {
    return (sys!=null && sys>=160) || (dia!=null && dia>=110);
  }
  public static boolean hypertension(Integer sys, Integer dia) {
    return (sys!=null && sys>=140) || (dia!=null && dia>=90);
  }
  /** Advisory risk suggestion from visit findings — never applied without
   *  physician confirmation (controller exposes it as a suggestion only). */
  public static String riskSuggestion(Integer sys, Integer dia, boolean proteinuria, boolean bleeding,
                                      boolean diabetes, int fetusCount, Integer motherAge, boolean previousCesarean,
                                      boolean severeSymptoms) {
    if (severePreeclampsia(sys,dia) || bleeding || severeSymptoms) return "critical";
    if (hypertension(sys,dia) || proteinuria || diabetes || fetusCount>1
        || (motherAge!=null && (motherAge<18 || motherAge>35)) || previousCesarean) return "high";
    return "low";
  }
  /** Postpartum haemorrhage: >=500 ml vaginal/assisted, >=1000 ml cesarean. */
  public static boolean isPph(int bloodLossMl, String deliveryType) {
    if ("cesarean".equals(deliveryType)) return bloodLossMl >= 1000;
    return bloodLossMl >= 500;
  }
  /** Postpartum danger signs block discharge until reviewed. */
  public static boolean dischargeBlocked(String bleedingStatus, Integer sys, Integer dia, Integer painScore, String mentalState) {
    if ("heavy".equals(bleedingStatus)) return true;
    if (hypertension(sys,dia)) return true;
    if (painScore!=null && painScore>=8) return true;
    return "distressed".equals(mentalState);
  }
}
