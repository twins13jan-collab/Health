package eg.gov.health.platform.identity;

import java.time.OffsetDateTime;

/** Login lockout rules: after MAX_FAILURES consecutive failed attempts the
 *  account locks for LOCK_MINUTES. Pure logic, unit-tested. */
public final class LockoutPolicy {
  public static final int MAX_FAILURES = 5;
  public static final long LOCK_MINUTES = 15;
  private LockoutPolicy() {}

  public static boolean isLocked(OffsetDateTime lockedUntil, OffsetDateTime now) {
    return lockedUntil != null && lockedUntil.isAfter(now);
  }

  /** New failure count after one more failed attempt. */
  public static int nextFailureCount(int current) {
    return current + 1;
  }

  /** Whether this failure count should trigger a lock. */
  public static boolean shouldLock(int failureCount) {
    return failureCount >= MAX_FAILURES;
  }

  /** Settings-driven variant: thresholds come from the platform
   *  settings panel (security.lockout_max_failures). */
  public static boolean shouldLock(int failureCount, int maxFailures) {
    return failureCount >= maxFailures;
  }

  public static OffsetDateTime lockUntil(OffsetDateTime now) {
    return now.plusMinutes(LOCK_MINUTES);
  }

  /** Settings-driven variant (security.lockout_minutes). */
  public static OffsetDateTime lockUntil(OffsetDateTime now, long minutes) {
    return now.plusMinutes(minutes);
  }
}
