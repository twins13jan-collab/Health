package eg.gov.health.platform.maternity;

import java.time.LocalDate;

/** Labour-ward board ordering (Phase 4.7, cycle 244). The board is an
 *  attention queue: a PPH emergency outranks everything, then a woman in
 *  active labour, then a critical-risk pregnancy, then an overdue
 *  pregnancy (past its expected delivery date and still undelivered),
 *  then high risk, then routine. Overdue is computed from the EDD the
 *  episode already carries — never from a re-derived date. */
public final class MaternityBoardRules {
  private MaternityBoardRules() {}

  /** Past the expected delivery date while the pregnancy has not ended. */
  public static boolean isOverdue(LocalDate edd, LocalDate today, String status) {
    if (edd == null || today == null) return false;
    if (!"ongoing".equals(status) && !"in_labor".equals(status)) return false;
    return today.isAfter(edd);
  }

  /** Lower rank = needs attention first. */
  public static int attentionRank(boolean pphEmergency, boolean inLabor, String riskLevel, boolean overdue) {
    if (pphEmergency) return 0;
    if (inLabor) return 1;
    if ("critical".equals(riskLevel)) return 2;
    if (overdue) return 3;
    if ("high".equals(riskLevel)) return 4;
    return 5;
  }
}
