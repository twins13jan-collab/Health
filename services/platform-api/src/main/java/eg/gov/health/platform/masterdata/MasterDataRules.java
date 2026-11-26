package eg.gov.health.platform.masterdata;
import java.util.Set;
/** Master data & reference catalogue rules (cycle 235). The domain
 *  owner defines meaning, the steward reviews quality, GOVERNANCE
 *  approves unified codes — a facility manages its permitted local
 *  values but never edits the national dictionary. An unknown code
 *  goes to review; it is never mapped by guessing. A deprecated code
 *  stays readable in historical records but cannot enter NEW
 *  transactions without an approved exception. Conflicts keep both
 *  sources with a documented unification decision. Quality is measured:
 *  unknown, duplicate and conflict rates. */
public final class MasterDataRules {
  private MasterDataRules() {}
  public static final Set<String> GOVERNANCE_ROLES = Set.of("data_governance","governance_officer","admin");
  public static boolean mayDecideStewardship(String role) { return role != null && GOVERNANCE_ROLES.contains(role); }
  /** Usage gate for a code in a new transaction. */
  public static boolean usableInNewTransaction(String codeStatus, boolean approvedException) {
    if ("active".equals(codeStatus)) return true;
    if ("deprecated".equals(codeStatus)) return approvedException;
    return false; // unknown_review is never usable until reviewed
  }
  /** Mapping an unknown code needs an approved stewardship case — never a guess. */
  public static boolean mappingAllowed(String codeStatus, boolean stewardshipApproved) {
    return !"unknown_review".equals(codeStatus) || stewardshipApproved;
  }
}
