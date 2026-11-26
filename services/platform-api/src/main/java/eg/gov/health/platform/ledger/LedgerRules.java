package eg.gov.health.platform.ledger;
import java.math.BigDecimal; import java.util.*;
/** General ledger rules (cycle 229). An unbalanced journal is refused.
 *  A posted journal is never edited — correction happens only through a
 *  reversing journal. The operational user creates the source
 *  transaction; the accountant reviews; the FINANCE MANAGER posts and
 *  closes. A closed period reopens only with approval, a reason and an
 *  audit trail. A bank movement without a reference stays unmatched.
 *  Foreign-currency entries keep their FX source and date. The trial
 *  balance is reproduced from posted journals alone. Nobody edits
 *  balances directly — there is no balance column to edit. */
public final class LedgerRules {
  private LedgerRules() {}
  public static final Set<String> POST_ROLES = Set.of("finance_manager","cfo","admin");
  public static boolean mayPost(String role) { return role != null && POST_ROLES.contains(role); }
  public static boolean balanced(List<BigDecimal[]> lines) {
    if (lines == null || lines.isEmpty()) return false;
    BigDecimal d = BigDecimal.ZERO, c = BigDecimal.ZERO;
    for (var l : lines) { d = d.add(l[0]); c = c.add(l[1]); }
    return d.signum() > 0 && d.compareTo(c) == 0;
  }
  public static boolean periodPostable(String periodStatus) {
    return "open".equals(periodStatus) || "reopened".equals(periodStatus);
  }
  public static boolean matchable(String bankReference, String journalReference, BigDecimal bankAmount, BigDecimal journalAmount) {
    return bankReference != null && !bankReference.isBlank()
        && bankReference.equals(journalReference)
        && bankAmount != null && journalAmount != null && bankAmount.compareTo(journalAmount) == 0;
  }
}
