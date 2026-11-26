package eg.gov.health.platform.traceability;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class TraceabilityRulesTest {
  @Test void oneEntityOneOwner() {
    assertTrue(TraceabilityRules.ownershipConflict("المختبر", "الصيدلة"));
    assertFalse(TraceabilityRules.ownershipConflict("المختبر", "المختبر"));
    assertFalse(TraceabilityRules.ownershipConflict(null, "الصيدلة"));
    assertTrue(TraceabilityRules.transferAuthorized("طبيب إحالة، منسق نقل"));
    assertFalse(TraceabilityRules.transferAuthorized(" "));
  }
}
