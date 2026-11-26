package eg.gov.health.platform.palliative;
import java.util.Set;
/** Palliative care rules (cycle 247). The system NEVER replaces the
 *  patient's wish or the physician's decision with an algorithm, and never
 *  creates a treatment or legal directive on the patient's behalf:
 *  a representative's authority must be legally verified, never assumed;
 *  documents are recorded with source, scope and review date; a newer
 *  UNVERIFIED document cannot supersede a verified one. */
public final class PalliativeRules {
  private PalliativeRules() {}
  /** Pain bands (0-10 numeric rating). */
  public static String painBand(Integer score) {
    if (score == null) return "unrecorded";
    if (score < 0 || score > 10) throw new IllegalArgumentException("pain score out of range");
    if (score == 0) return "none";
    if (score <= 3) return "mild";
    if (score <= 6) return "moderate";
    return "severe";
  }
  /** Severe pain/symptom escalation needs an urgent protocol response. */
  public static boolean urgentResponseRequired(Integer painScore) {
    return painScore != null && painScore >= 7;
  }
  /** Symptom relief = measured before/after change (points). */
  public static int reliefDelta(int painBefore, int painAfter) { return painBefore - painAfter; }
  public static boolean reliefAchieved(int painBefore, int painAfter) { return reliefDelta(painBefore, painAfter) >= 2; }
  /** Sources from which a goals-of-care record may be documented. */
  public static final Set<String> VALID_SOURCES = Set.of("patient_signed","patient_verbal_witnessed","court_order","legal_representative");
  /** A record naming a representative is acceptable only when that
   *  representative's legal authority is verified — never assumed. */
  public static boolean representativeAcceptable(String representativeName, boolean verified, String source) {
    if (!VALID_SOURCES.contains(source)) return false;
    if (representativeName == null || representativeName.isBlank()) return true; // patient speaks for themselves
    return verified && "legal_representative".equals(source) || verified && "court_order".equals(source);
  }
  /** Supersession: a new document may supersede the active one only if it
   *  is legally verified whenever the active one is. */
  public static boolean maySupersede(boolean activeVerified, boolean newVerified) {
    return newVerified || !activeVerified;
  }
}
