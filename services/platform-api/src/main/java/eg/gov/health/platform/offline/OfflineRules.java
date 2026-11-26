package eg.gov.health.platform.offline;
import java.time.*; import java.util.Set;
/** Offline work & sync rules (cycle 237). Local privilege never exceeds
 *  central privilege: work happens inside a bounded package (device,
 *  user, scope, duration). Sync is encrypted, signed and IDEMPOTENT —
 *  replaying an envelope never duplicates a transaction. A lost device
 *  is revoked and its institutional data is wiped on contact. Stale
 *  reference data halts operations that need fresh verification.
 *  Conflicts in clinical or financial domains are NEVER resolved by
 *  automatic last-write-wins; they go to the facility manager with a
 *  domain rule. */
public final class OfflineRules {
  private OfflineRules() {}
  /** Operation types that must halt on stale reference data. */
  public static final Set<String> FRESH_REQUIRED = Set.of("dispense","payment","allergy_check","dose_check");
  /** The reference-data version the platform currently requires for fresh checks. */
  public static final int CURRENT_REFERENCE_VERSION = 3;
  public static boolean packageUsable(String pkgStatus, OffsetDateTime validUntil, OffsetDateTime now,
                                      String deviceStatus, boolean encryptionEnabled) {
    return "active".equals(pkgStatus) && "trusted".equals(deviceStatus) && encryptionEnabled
        && validUntil != null && now != null && now.isBefore(validUntil);
  }
  public static boolean freshEnough(String operationType, int txReferenceVersion) {
    return !FRESH_REQUIRED.contains(operationType) || txReferenceVersion >= CURRENT_REFERENCE_VERSION;
  }
  /** Clinical/financial conflicts never auto-resolve; administrative may, by stated rule. */
  public static boolean autoResolvable(String domain) {
    return "administrative".equals(domain);
  }
  public static boolean withinScope(String packageScope, String operationType) {
    if (packageScope == null || packageScope.isBlank() || "*".equals(packageScope.trim())) return true;
    for (String s : packageScope.split(",")) if (s.trim().equals(operationType)) return true;
    return false;
  }
}
