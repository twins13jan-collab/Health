package eg.gov.health.platform.finance;
/** Attention rules for the finance board (cycle 228 operations
 *  layer). Money owed to the facility ages in public: a rejected
 *  invoice needs correction before anything else; a draft left
 *  unissued for a week is work leaking; receivables then rank by
 *  age — over 90 days, over 30, claimed with the payer — while
 *  paid and cancelled invoices leave the queue entirely. */
public final class FinanceBoardRules {
  private FinanceBoardRules() {}
  public static final int DRAFT_STALE_DAYS = 7;

  public static String agingBucket(long ageDays) {
    if (ageDays <= 30) return "current";
    if (ageDays <= 60) return "d31_60";
    if (ageDays <= 90) return "d61_90";
    return "over_90";
  }
  public static boolean receivable(String status) {
    return "issued".equals(status) || "claimed".equals(status) || "partially_paid".equals(status);
  }
  /** 0 = act now … 9 = settled business (off the queue). */
  public static int invoiceRank(String status, long ageDays) {
    if ("rejected".equals(status)) return 0;
    if ("draft".equals(status) && ageDays > DRAFT_STALE_DAYS) return 1;
    if (receivable(status) && ageDays > 90) return 2;
    if (receivable(status) && ageDays > 30) return 3;
    if ("claimed".equals(status)) return 4;
    if ("draft".equals(status)) return 5;
    if (receivable(status)) return 6;
    return 9;
  }
}
