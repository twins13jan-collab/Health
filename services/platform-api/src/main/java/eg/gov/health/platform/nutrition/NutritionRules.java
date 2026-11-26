package eg.gov.health.platform.nutrition;
/** Clinical nutrition rules (cycle 251). The dietitian assesses and
 *  proposes; the physician approves the prescription; the pharmacist
 *  reviews parenteral compatibility before it starts; nursing records
 *  what was ACTUALLY given — the prescribed-vs-given gap is shown with
 *  its reason, never rewritten away; an unavailable component is never
 *  substituted automatically; intolerance/aspiration follows an approved
 *  protocol and holds the feed. */
public final class NutritionRules {
  private NutritionRules() {}
  public static double bmi(double weightKg, double heightCm) {
    if (heightCm <= 0) throw new IllegalArgumentException("height required");
    double m = heightCm / 100.0;
    return Math.round(weightKg / (m * m) * 10.0) / 10.0;
  }
  /** Screening risk from BMI and unintentional weight loss. */
  public static String riskLevel(double bmi, Double weightLossPercent) {
    double loss = weightLossPercent == null ? 0 : weightLossPercent;
    if (bmi < 18.5 || loss > 10.0) return "high";
    if (bmi < 20.0 || loss > 5.0) return "moderate";
    return "low";
  }
  /** Suggested energy range: 25-30 kcal/kg/day. */
  public static int[] needsRangeKcal(double weightKg) {
    return new int[]{ (int) Math.round(25 * weightKg), (int) Math.round(30 * weightKg) };
  }
  public static boolean needsOutOfRange(Integer kcal, double weightKg) {
    if (kcal == null) return false;
    int[] r = needsRangeKcal(weightKg);
    return kcal < r[0] || kcal > r[1];
  }
  /** Orders exist only under an approved plan. */
  public static boolean ordersAllowed(String planStatus) { return "approved".equals(planStatus) || "active".equals(planStatus); }
  /** Parenteral nutrition starts only after pharmacist review. */
  public static boolean mayStart(String kind, boolean pharmacistReviewed) {
    return !"parenteral".equals(kind) || pharmacistReviewed;
  }
  /** The prescribed-vs-given gap: a shortfall or an early stop needs a reason. */
  public static boolean gapNeedsReason(double prescribedMl, double actualMl, boolean stopped, String reason) {
    boolean gap = actualMl < prescribedMl || stopped;
    return gap && (reason == null || reason.isBlank());
  }
  public static double varianceMl(double prescribedMl, double actualMl) {
    return Math.round((prescribedMl - actualMl) * 10.0) / 10.0;
  }
  /** Complications that hold the feed under protocol. */
  public static boolean holdsFeed(String type) { return "aspiration".equals(type) || "intolerance".equals(type); }
}
