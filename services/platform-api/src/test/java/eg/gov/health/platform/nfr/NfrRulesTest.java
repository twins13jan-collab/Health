package eg.gov.health.platform.nfr;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
class NfrRulesTest {
  @Test void nfrsAreMetOnlyByFreshEvidence() {
    var today = LocalDate.parse("2026-11-22");
    assertEquals("met", NfrRules.status(99.95, 99.9, "at_least", LocalDate.parse("2026-11-20"), today));
    assertEquals("unmet", NfrRules.status(98.0, 99.9, "at_least", LocalDate.parse("2026-11-20"), today));
    assertEquals("unverified", NfrRules.status(99.99, 99.9, "at_least", LocalDate.parse("2026-08-01"), today)); // stale proof
    assertEquals("unverified", NfrRules.status(null, 99.9, "at_least", null, today));
    assertTrue(NfrRules.withinTarget(180.0, 200.0, "at_most"));
    assertFalse(NfrRules.withinTarget(240.0, 200.0, "at_most"));
  }
}
