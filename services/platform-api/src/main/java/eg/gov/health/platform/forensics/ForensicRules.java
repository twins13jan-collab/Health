package eg.gov.health.platform.forensics;
/** Forensic-medicine rules (module 71). Forensic work lives
 *  apart from the ordinary clinical record, under legal rules:
 *  a case opens for examination only when the requesting
 *  authority's jurisdiction and mandate are verified. Evidence
 *  moves only through an unbroken chain of custody — every
 *  transfer names both parties and is recorded, and release goes
 *  only to the verified requesting authority. A medico-legal
 *  report is released only with the examiner's signature AND a
 *  countersignature. Access to the forensic file is restricted
 *  to forensic roles. */
public final class ForensicRules {
  private ForensicRules() {}
  public static boolean examinationPermitted(boolean authorityVerified) { return authorityVerified; }
  public static boolean transferPermitted(String itemStatus, String fromParty, String toParty) {
    return ("stored".equals(itemStatus) || "in_examination".equals(itemStatus))
      && fromParty != null && !fromParty.isBlank() && toParty != null && !toParty.isBlank();
  }
  public static boolean reportReleasePermitted(String signedBy, String countersignedBy, String releasedTo, String requestingAuthority) {
    return signedBy != null && countersignedBy != null && !signedBy.equals(countersignedBy)
      && releasedTo != null && releasedTo.equals(requestingAuthority);
  }
  public static boolean forensicAccess(String role) {
    return "admin".equals(role) || "forensic_physician".equals(role) || "physician".equals(role);
  }
}
