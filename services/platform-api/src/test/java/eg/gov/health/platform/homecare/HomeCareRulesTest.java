package eg.gov.health.platform.homecare;
import eg.gov.health.platform.criticalcare.IcuRules;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
class HomeCareRulesTest {
  @Test void visitsNeedActivePlan() {
    assertTrue(HomeCareRules.visitsAllowed("active"));
    assertFalse(HomeCareRules.visitsAllowed("closed"));
  }
  @Test void visitTimesOrdered() {
    var t = OffsetDateTime.now();
    assertTrue(HomeCareRules.visitTimesValid(t, t.plusHours(1)));
    assertFalse(HomeCareRules.visitTimesValid(t, t.minusMinutes(5)));
    assertFalse(HomeCareRules.visitTimesValid(null, t));
  }
  @Test void deteriorationEscalates() {
    assertTrue(HomeCareRules.escalationRequired("deteriorated"));
    assertTrue(HomeCareRules.escalationRequired("emergency"));
    assertFalse(HomeCareRules.escalationRequired("stable"));
  }
  @Test void maintenanceOverdueAfterDueDate() {
    assertTrue(HomeCareRules.maintenanceOverdue(LocalDate.of(2026,11,1), LocalDate.of(2026,11,22)));
    assertFalse(HomeCareRules.maintenanceOverdue(LocalDate.of(2026,12,1), LocalDate.of(2026,11,22)));
  }
  @Test void remoteFreshnessSameLawAsIcu() {
    var now = OffsetDateTime.now();
    assertEquals(IcuRules.Freshness.LIVE, HomeCareRules.freshness(now.minusSeconds(30), now));
    assertEquals(IcuRules.Freshness.SIGNAL_LOST, HomeCareRules.freshness(now.minusMinutes(20), now));
    assertFalse(HomeCareRules.mayPresentAsLive(IcuRules.Freshness.SIGNAL_LOST));
  }
}
