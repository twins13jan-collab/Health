package eg.gov.health.platform.adt;
import org.junit.jupiter.api.Test;
import java.time.OffsetDateTime; import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class AdtRulesTest {
  @Test void bedNeverDoubleBooked() {
    assertTrue(AdtRules.bedFree(List.of("1", "2"), "3"));
    assertFalse(AdtRules.bedFree(List.of("1", "2"), "2"));
    assertFalse(AdtRules.bedFree(List.of("A-1"), "a-1")); // case-insensitive labels
    assertFalse(AdtRules.bedFree(List.of(), " "));
    assertFalse(AdtRules.bedFree(List.of(), null));
  }
  @Test void wardCapacityIsPhysical() {
    assertTrue(AdtRules.wardHasCapacity(10, 9));
    assertFalse(AdtRules.wardHasCapacity(10, 10));
    assertFalse(AdtRules.wardHasCapacity(0, 0));
  }
  @Test void dischargeNeedsKnownDispositionAndActiveStay() {
    assertTrue(AdtRules.validDisposition("home"));
    assertTrue(AdtRules.validDisposition("ama"));
    assertFalse(AdtRules.validDisposition("escaped"));
    assertFalse(AdtRules.validDisposition(null));
    assertTrue(AdtRules.dischargeable("admitted"));
    assertFalse(AdtRules.dischargeable("discharged"));
  }
  @Test void transferNeedsReasonAndRealMove() {
    var w1 = UUID.randomUUID(); var w2 = UUID.randomUUID();
    assertEquals("transfer_reason_required", AdtRules.transferProblem(w1, "1", w2, "4", " "));
    assertEquals("transfer_same_bed", AdtRules.transferProblem(w1, "1", w1, "1", "تدهور الحالة"));
    assertNull(AdtRules.transferProblem(w1, "1", w1, "2", "قرب من التمريض"));
    assertNull(AdtRules.transferProblem(w1, "1", w2, "4", "يحتاج عناية مركزة"));
    assertTrue(AdtRules.validSource("er"));
    assertFalse(AdtRules.validSource("walkin"));
    assertTrue(AdtRules.validSource(null)); // defaults to clinic
  }
  @Test void lengthOfStayNeverNegative() {
    var t = OffsetDateTime.now();
    assertEquals(3, AdtRules.lengthOfStayDays(t.minusDays(3).minusHours(2), t));
    assertEquals(0, AdtRules.lengthOfStayDays(t.plusDays(1), t));
    assertEquals(0, AdtRules.lengthOfStayDays(null, t));
  }
}
