package eg.gov.health.platform.facilitysafety;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
class SafetyRulesTest {
  @Test void permitsAreIndependentlyApprovedWithControls() {
    assertTrue(SafetyRules.permitApprovable("tech@x", "safety@x", "welding", true, false));
    assertFalse(SafetyRules.permitApprovable("tech@x", "tech@x", "welding", true, false)); // self-approval
    assertFalse(SafetyRules.permitApprovable("tech@x", "safety@x", "welding", false, false)); // no fire watch
    assertFalse(SafetyRules.permitApprovable("tech@x", "safety@x", "electrical_isolation", false, false)); // no LOTO
    assertTrue(SafetyRules.permitApprovable("tech@x", "safety@x", "electrical_isolation", false, true));
  }
  @Test void sensitiveAccessIsEscortedAndTimeBoxed() {
    var from = OffsetDateTime.parse("2026-11-22T08:00+02:00"); var until = OffsetDateTime.parse("2026-11-22T16:00+02:00");
    var at = OffsetDateTime.parse("2026-11-22T10:00+02:00");
    assertTrue(SafetyRules.accessAllowed("contractor", true, "مرافق أمن", from, until, at));
    assertFalse(SafetyRules.accessAllowed("visitor", true, null, from, until, at)); // unescorted visitor
    assertFalse(SafetyRules.accessAllowed("contractor", true, "مرافق", from, until, OffsetDateTime.parse("2026-11-22T18:00+02:00"))); // expired
    assertTrue(SafetyRules.accessAllowed("staff", true, null, from, until, at));
  }
}
