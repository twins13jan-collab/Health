package eg.gov.health.platform.mentalhealth;
import java.util.Set;
/** Mental-health & addiction rules (cycle 252) under STRICT confidentiality:
 *  access is limited to the treating team; the diagnosis is never exposed in
 *  list views, public boards or unauthorised reports; every read is audited;
 *  an imminent risk activates the safety protocol and a safety plan is
 *  mandatory at high risk or above; emergency summaries carry the minimum
 *  necessary only. */
public final class MentalHealthRules {
  private MentalHealthRules() {}
  /** Roles that may read a mental-health record (treating need-to-know). */
  public static final Set<String> TREATING_ROLES = Set.of("psychiatrist","psychologist","mh_nurse","privacy_officer","admin");
  public static boolean canAccess(String role) { return role != null && TREATING_ROLES.contains(role); }
  /** PHQ-9 severity bands (0-27). */
  public static String phq9Severity(int score) {
    if (score < 0 || score > 27) throw new IllegalArgumentException("PHQ-9 out of range");
    if (score <= 4) return "minimal";
    if (score <= 9) return "mild";
    if (score <= 14) return "moderate";
    if (score <= 19) return "moderately_severe";
    return "severe";
  }
  /** GAD-7 severity bands (0-21). */
  public static String gad7Severity(int score) {
    if (score < 0 || score > 21) throw new IllegalArgumentException("GAD-7 out of range");
    if (score <= 4) return "minimal";
    if (score <= 9) return "mild";
    if (score <= 14) return "moderate";
    return "severe";
  }
  public static String severityFor(String instrument, int score) {
    return switch (instrument) {
      case "PHQ-9" -> phq9Severity(score);
      case "GAD-7" -> gad7Severity(score);
      default -> "unbanded";
    };
  }
  /** A safety plan is mandatory at high or imminent risk. */
  public static boolean safetyPlanRequired(String riskLevel) {
    return "high".equals(riskLevel) || "imminent".equals(riskLevel);
  }
  /** Imminent risk activates the safety protocol immediately. */
  public static boolean activatesSafetyProtocol(String riskLevel) { return "imminent".equals(riskLevel); }
  /** CIWA-Ar withdrawal bands: 0-8 mild, 9-15 moderate, 16-20 severe, >20 very severe. */
  public static String ciwaBand(int score) {
    if (score < 0) throw new IllegalArgumentException("CIWA out of range");
    if (score <= 8) return "mild";
    if (score <= 15) return "moderate";
    if (score <= 20) return "severe";
    return "very_severe";
  }
  public static boolean ciwaNeedsMedicalMonitoring(int score) { return score >= 16; }
}
