package eg.gov.health.platform.insurancedeep;
import java.util.*;
/** Attention rules for the insurance-claims board (module 113
 *  operations layer). A fraud flag awaiting human review blocks
 *  settlement and tops the queue — a flag is a suspicion, not a
 *  verdict, but nothing settles past it. Then open appeals, then
 *  claims that cannot be adjudicated yet (their documentation or
 *  tariff gaps are named, never hidden), then undecided claims,
 *  then decided claims awaiting settlement. */
public final class ClaimsBoardRules {
  private ClaimsBoardRules() {}
  public static List<String> gaps(boolean documentationComplete, boolean tariffMatch) {
    List<String> g = new ArrayList<>();
    if (!documentationComplete) g.add("documentation_incomplete");
    if (!tariffMatch) g.add("tariff_mismatch");
    return g;
  }
  public static String claimState(boolean fraudFlag, boolean fraudReviewed, boolean openAppeal,
                                  boolean adjudicable, boolean decided, boolean settled) {
    if (settled) return "settled";
    if (fraudFlag && !fraudReviewed) return "fraud_review_pending";
    if (openAppeal) return "appeal_open";
    if (!adjudicable) return "not_adjudicable";
    if (!decided) return "awaiting_decision";
    return "awaiting_settlement";
  }
  /** 0 = act now … 9 = settled (off the queue). */
  public static int claimRank(String state) {
    return switch (state) {
      case "fraud_review_pending" -> 0;
      case "appeal_open" -> 1;
      case "not_adjudicable" -> 2;
      case "awaiting_decision" -> 3;
      case "awaiting_settlement" -> 4;
      default -> 9;
    };
  }
}
