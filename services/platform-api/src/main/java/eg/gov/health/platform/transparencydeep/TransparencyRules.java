package eg.gov.health.platform.transparencydeep;
import java.util.*;
/** Transparency rules (module 140). Public information is
 *  published only lawfully and completely labelled: source,
 *  update date and methodology shown, and never containing
 *  sensitive data — transparency about the system is not
 *  exposure of its patients. Published information found
 *  inaccurate is corrected with a visible note or withdrawn;
 *  it is never silently edited, because a public record that
 *  changes without a trace teaches the public to trust nothing
 *  it publishes. */
public final class TransparencyRules {
  private TransparencyRules() {}
  public static List<String> publishBlockers(String sourceRef, String methodology, String legalBasis, boolean containsSensitive) {
    List<String> b = new ArrayList<>();
    if (sourceRef == null || sourceRef.isBlank()) b.add("source_missing");
    if (methodology == null || methodology.isBlank()) b.add("methodology_missing");
    if (legalBasis == null || legalBasis.isBlank()) b.add("legal_basis_missing");
    if (containsSensitive) b.add("contains_sensitive_data");
    return b;
  }
  public static boolean correctionValid(String correctionNote) {
    return correctionNote != null && !correctionNote.isBlank();
  }
}
