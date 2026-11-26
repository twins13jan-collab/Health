package eg.gov.health.platform.porterage;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
class PorterageRulesTest {
  @Test void criticalAndOxygenNeedANamedEscort() {
    assertFalse(PorterageRules.escortSatisfied("critical", false, null));
    assertTrue(PorterageRules.escortSatisfied("critical", false, "ممرضة أمل"));
    assertFalse(PorterageRules.escortSatisfied("routine", true, null)); // oxygen always escorts
    assertTrue(PorterageRules.escortSatisfied("routine", false, null));
  }
  @Test void responseTargetsArePerPriority() {
    var req = OffsetDateTime.parse("2026-11-22T10:00:00+02:00");
    assertFalse(PorterageRules.responseBreached("critical", req, req.plusMinutes(9)));
    assertTrue(PorterageRules.responseBreached("critical", req, req.plusMinutes(14)));
    assertFalse(PorterageRules.responseBreached("routine", req, req.plusMinutes(14)));
  }
  @Test void roomReadyNeedsAllThree() {
    assertTrue(PorterageRules.roomReady(true, true, "مشرفة نظافة"));
    assertFalse(PorterageRules.roomReady(true, true, null));
    assertFalse(PorterageRules.roomReady(true, false, "مشرفة"));
  }
  @Test void laundryLossIsCounted() {
    assertEquals(3, PorterageRules.laundryLoss(50, 47));
    assertEquals(0, PorterageRules.laundryLoss(50, 50));
  }
}
