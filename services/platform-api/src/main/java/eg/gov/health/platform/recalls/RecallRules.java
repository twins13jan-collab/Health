package eg.gov.health.platform.recalls;
import java.time.*;
/** Drug batch & recall rules (cycle 226). Balances live in an append-only
 *  ledger bound to batch + location + movement + document + cost; nobody
 *  edits a balance directly — a count difference becomes an APPROVED
 *  adjustment movement. Expired, quarantined or recalled stock never
 *  dispenses. FEFO applies: dispensing a later-expiry batch while an
 *  earlier one holds stock is an exception that must be documented.
 *  A published recall blocks dispensing everywhere, collects a receipt
 *  per location, and closes only when dispositions cover what was
 *  quarantined. An external notification counts as sent only on ACK. */
public final class RecallRules {
  private RecallRules() {}
  public static boolean dispensable(String releaseStatus, LocalDate expiryDate, LocalDate today) {
    return "released".equals(releaseStatus) && expiryDate != null && today != null && !today.isAfter(expiryDate);
  }
  /** FEFO exception: chosen batch expires later than the earliest stocked released batch. */
  public static boolean fefoException(LocalDate chosenExpiry, LocalDate earliestStockedExpiry) {
    return earliestStockedExpiry != null && chosenExpiry != null && chosenExpiry.isAfter(earliestStockedExpiry);
  }
  public static boolean exceptionDocumented(boolean isException, String reason) {
    return !isException || (reason != null && !reason.isBlank());
  }
  /** Adjustments (inventory differences) need quality approval. */
  public static boolean movementAllowed(String type, String approvedBy) {
    return !"adjustment".equals(type) || (approvedBy != null && !approvedBy.isBlank());
  }
  /** Recall closure: every receipted location's quarantined quantity is disposed of. */
  public static boolean recallClosable(long totalQuarantined, long totalDisposed) {
    return totalQuarantined <= totalDisposed;
  }
}
