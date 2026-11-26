package eg.gov.health.platform.purchasing;
import java.math.BigDecimal;
/** Procurement rules (cycle 227). Duties are separated end to end: the
 *  requester never approves their own purchase, the evaluation
 *  committee scores, procurement issues only after approvals, and
 *  technical/warehouse receiving is separate from issuing and from
 *  payment. Partial or substitute delivery needs BOTH technical and
 *  contractual approval. Payment requires the three-way match
 *  (order = receipt = invoice) under an accepted inspection — a
 *  mismatching invoice is HELD, never paid. A supplier bank change
 *  needs independent verification by a different person. A contract
 *  change is an addendum version; the previous version is preserved. */
public final class PurchasingRules {
  private PurchasingRules() {}
  public static boolean approvable(String requester, String approver) {
    return approver != null && !approver.isBlank() && !approver.equals(requester);
  }
  public static boolean deliveryApprovable(boolean partial, boolean substitute, String technicalApproval, String contractApproval) {
    if (!partial && !substitute) return true;
    return technicalApproval != null && !technicalApproval.isBlank()
        && contractApproval != null && !contractApproval.isBlank();
  }
  public static boolean receiverIndependent(String poIssuer, String receiver) {
    return receiver != null && !receiver.isBlank() && !receiver.equals(poIssuer);
  }
  public static boolean threeWayMatch(BigDecimal poAmount, BigDecimal invoicedAmount, String inspectionResult) {
    return poAmount != null && invoicedAmount != null && poAmount.compareTo(invoicedAmount) == 0
        && "accepted".equals(inspectionResult);
  }
  public static boolean bankChangeVerified(String changedBy, String verifiedBy) {
    return verifiedBy != null && !verifiedBy.isBlank() && !verifiedBy.equals(changedBy);
  }
  public static boolean payerIndependent(String poIssuer, String receiver, String payer) {
    return payer != null && !payer.isBlank() && !payer.equals(poIssuer) && !payer.equals(receiver);
  }
}
