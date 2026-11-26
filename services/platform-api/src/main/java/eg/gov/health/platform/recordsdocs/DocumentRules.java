package eg.gov.health.platform.recordsdocs;
import java.time.*; import java.util.Set;
/** Document & retention rules (cycle 239). Every document has an
 *  owner, a classification, a version, a fingerprint and a source.
 *  The creator uploads versions but never deletes the approved one.
 *  A signature counts as approval only when its certificate verifies
 *  AND it was made within the signer's scope for that document type;
 *  an invalid signature is not an approval. Editing after signing
 *  creates a NEW version and the old signature does not carry over.
 *  A legally held or retention-bound record is never deleted — a
 *  deletion request against it is HELD with the basis documented.
 *  A corrupted file keeps its original and opens a restoration
 *  request. The records manager runs retention, never the medical
 *  content. Viewing, printing, downloading and sensitive sharing are
 *  all recorded with a purpose. */
public final class DocumentRules {
  private DocumentRules() {}
  public static final Set<String> RETENTION_ROLES = Set.of("records_manager","admin");
  public static boolean mayManageRetention(String role) { return role != null && RETENTION_ROLES.contains(role); }
  public static boolean signatureCounts(boolean certificateValid, boolean inScope) {
    return certificateValid && inScope;
  }
  public static boolean signatureAppliesTo(String signedHash, String currentHash) {
    return signedHash != null && signedHash.equals(currentHash);
  }
  public static boolean deletionBlocked(boolean legalHoldActive, LocalDate retentionUntil, LocalDate today) {
    if (legalHoldActive) return true;
    return retentionUntil != null && today != null && today.isBefore(retentionUntil);
  }
}
