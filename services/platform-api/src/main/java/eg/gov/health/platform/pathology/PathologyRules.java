package eg.gov.health.platform.pathology;
/** Pathology rules (cycle 240). An unidentified or mismatched specimen is
 *  quarantined — identity is never guessed. An insufficient specimen is
 *  documented and recollection is a clinical decision. Preliminary and
 *  final reports are different things; a final report is signed by an
 *  authorised pathologist; an approved report is NEVER edited — an
 *  addendum/correction is issued, supersedes it, and the recipients of
 *  the previous version are notified. Two specialists' opinions are both
 *  preserved; review decides, nothing is erased. */
public final class PathologyRules {
  private PathologyRules() {}
  /** Processing may proceed only on an accepted, identity-verified specimen. */
  public static boolean processable(String accessionStatus) { return "accepted".equals(accessionStatus); }
  /** Receipt: unverified identity goes straight to quarantine. */
  public static String receiptStatus(boolean identityVerified) { return identityVerified ? "received" : "quarantined"; }
  /** Insufficiency must be documented to be recorded. */
  public static boolean insufficiencyDocumented(String note) { return note != null && !note.isBlank(); }
  /** A final report requires a signature by its pathologist author. */
  public static boolean finalReportValid(String kind, boolean signed, String author) {
    return !"final".equals(kind) || (signed && author != null && !author.isBlank());
  }
  /** Amending anything already issued requires a documented reason. */
  public static boolean amendmentValid(String reason) { return reason != null && !reason.isBlank(); }
}
