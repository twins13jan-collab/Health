package eg.gov.health.platform.programs;
import java.math.*;
/** Chemotherapy dose computation (oncology program): body-surface area by
 *  the Mosteller formula; dose = BSA x protocol mg/m2, with renal dose
 *  adjustment bands by creatinine clearance. Computed values are advisory
 *  to the treating oncologist and the verifying pharmacist — the system
 *  never administers; it computes, checks caps, and records. */
public final class ChemoDosing {
  private ChemoDosing() {}
  public static double bsaMosteller(double heightCm, double weightKg) {
    if (heightCm <= 0 || weightKg <= 0) throw new IllegalArgumentException("height/weight must be positive");
    return Math.sqrt(heightCm * weightKg / 3600.0);
  }
  public static BigDecimal doseMg(double bsa, BigDecimal mgPerM2) {
    return mgPerM2.multiply(BigDecimal.valueOf(bsa)).setScale(1, RoundingMode.HALF_UP);
  }
  /** Dose fraction by creatinine clearance (ml/min): >=60 full, 40-59 75%, 25-39 50%, <25 contraindicated review. */
  public static BigDecimal renalAdjustmentPct(int creatinineClearance) {
    if (creatinineClearance >= 60) return new BigDecimal("100");
    if (creatinineClearance >= 40) return new BigDecimal("75");
    if (creatinineClearance >= 25) return new BigDecimal("50");
    return BigDecimal.ZERO; // below 25: no automatic dose — specialist review mandatory
  }
  public static BigDecimal adjustedDoseMg(double bsa, BigDecimal mgPerM2, int crCl) {
    var pct = renalAdjustmentPct(crCl);
    if (pct.signum() == 0) return BigDecimal.ZERO;
    return doseMg(bsa, mgPerM2).multiply(pct).divide(new BigDecimal("100"), 1, RoundingMode.HALF_UP);
  }
}
