package eg.gov.health.platform.medicalrecords;
import java.time.*; import java.util.*;
/** Medical-records completeness rules (Phase 4.9). An encounter file
 *  is complete — and only then may it be filed (closed) — when it
 *  holds three things: at least one SIGNED clinical note, a FINAL
 *  closing summary, and ICD-10 coding. Missing items are named as
 *  deficiencies, never waved through: an incomplete file stays open
 *  on the board with its gaps visible. Files are due within
 *  FILING_SLA_DAYS of the encounter completing; past that they are
 *  overdue and top the board. */
public final class RecordRules {
  private RecordRules() {}
  public static final long FILING_SLA_DAYS = 14;
  public static List<String> deficiencies(boolean hasSignedNote, String summaryStatus, boolean coded) {
    var d = new ArrayList<String>();
    if (!hasSignedNote) d.add("clinical_note_missing");
    if (summaryStatus == null) d.add("discharge_summary_missing");
    else if (!"final".equals(summaryStatus)) d.add("discharge_summary_not_final");
    if (!coded) d.add("icd10_coding_missing");
    return d;
  }
  public static boolean icdFormatValid(String code) {
    return code != null && code.trim().toUpperCase().matches("^[A-Z][0-9]{2}(\\.[0-9A-Z]{1,2})?$");
  }
  public static boolean filingOverdue(OffsetDateTime completedAt, OffsetDateTime now) {
    return completedAt != null && now != null && Duration.between(completedAt, now).toDays() > FILING_SLA_DAYS;
  }
  /** Overdue files first, then files ready to close, then the rest. */
  public static int attentionRank(boolean ready, boolean overdue) {
    if (overdue) return 0;
    if (ready) return 1;
    return 2;
  }
}
