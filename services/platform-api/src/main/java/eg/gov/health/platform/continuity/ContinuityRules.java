package eg.gov.health.platform.continuity;
import java.time.*;
/** Continuity rules (module 123). A backup is a hope; a restore
 *  TEST is evidence. A critical service counts as
 *  recovery-proven only when an actual restore test met its RTO
 *  and RPO targets — and the proof expires after six months.
 *  Failover to alternate operations is a declared act with a
 *  recorded reason, never a silent drift. */
public final class ContinuityRules {
  private ContinuityRules() {}
  public static final int PROOF_VALID_MONTHS = 6;
  public static boolean restoreTestPassed(Integer actualRto, Integer actualRpo, Integer targetRto, Integer targetRpo, boolean success) {
    return success && actualRto != null && targetRto != null && actualRto <= targetRto
      && actualRpo != null && targetRpo != null && actualRpo <= targetRpo;
  }
  public static boolean recoveryProven(LocalDate lastPassedTest, LocalDate today) {
    return lastPassedTest != null && today != null && !lastPassedTest.plusMonths(PROOF_VALID_MONTHS).isBefore(today);
  }
}
