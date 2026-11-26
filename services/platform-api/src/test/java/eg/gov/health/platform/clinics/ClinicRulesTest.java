package eg.gov.health.platform.clinics;
import org.junit.jupiter.api.Test;
import java.time.*; import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class ClinicRulesTest {
  @Test void sessionValidation() {
    assertNull(ClinicRules.sessionProblem(LocalTime.of(9, 0), LocalTime.of(12, 0), 15, 12));
    assertNotNull(ClinicRules.sessionProblem(LocalTime.of(12, 0), LocalTime.of(9, 0), 15, 12));
    assertNotNull(ClinicRules.sessionProblem(LocalTime.of(9, 0), LocalTime.of(12, 0), 3, 12));
    assertNotNull(ClinicRules.sessionProblem(LocalTime.of(9, 0), LocalTime.of(12, 0), 15, 0));
    assertNotNull(ClinicRules.sessionProblem(LocalTime.of(9, 0), LocalTime.of(9, 10), 15, 12));
  }
  @Test void slotCounts() {
    assertEquals(12, ClinicRules.slotCount(LocalTime.of(9, 0), LocalTime.of(12, 0), 15));
    assertEquals(8, ClinicRules.slotCount(LocalTime.of(9, 0), LocalTime.of(11, 10), 15));
  }
  @Test void bookingRefusals() {
    var today = LocalDate.of(2026, 11, 23);
    assertNull(ClinicRules.bookingProblem("open", today, today, 3, 12, false));
    assertNotNull(ClinicRules.bookingProblem("cancelled", today, today, 0, 12, false));
    assertNotNull(ClinicRules.bookingProblem("closed", today, today, 0, 12, false));
    assertNotNull(ClinicRules.bookingProblem("open", today.minusDays(1), today, 0, 12, false));
    assertNotNull(ClinicRules.bookingProblem("open", today, today, 12, 12, false));
    assertNotNull(ClinicRules.bookingProblem("open", today, today, 3, 12, true));
  }
  @Test void promotionPutsUrgentFirstThenArrival() {
    var s = UUID.randomUUID(); var f = UUID.randomUUID();
    var normalEarly = new ClinicWaitlistEntry(s, f, null, UUID.randomUUID(), null, "normal");
    var urgentLate = new ClinicWaitlistEntry(s, f, null, UUID.randomUUID(), null, "urgent");
    var list = new ArrayList<>(List.of(normalEarly, urgentLate));
    list.sort(ClinicRules.promotionOrder());
    // createdAt is set at construction; both are ~now, so urgent must win on priority alone
    assertEquals("urgent", list.get(0).getPriority());
  }
  @Test void cancelNeedsReason() {
    assertNotNull(ClinicRules.cancelProblem(null));
    assertNotNull(ClinicRules.cancelProblem("  "));
    assertNull(ClinicRules.cancelProblem("طلب المريض"));
  }
}
