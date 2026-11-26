package eg.gov.health.platform.protocolversions;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
class ProtocolRulesTest {
  @Test void completeActivationAndPointInTimeForce() {
    assertTrue(ProtocolRules.activationBlockers("مرجع ESC 2025", "لجنة القلب", true, LocalDate.parse("2026-01-01")).isEmpty());
    assertTrue(ProtocolRules.activationBlockers("", "لجنة", true, LocalDate.parse("2026-01-01")).contains("scientific_ref_missing"));
    assertTrue(ProtocolRules.activationBlockers("مرجع", "لجنة", false, LocalDate.parse("2026-01-01")).contains("committee_unapproved"));
    assertTrue(ProtocolRules.inForce("active", LocalDate.parse("2026-01-01"), LocalDate.parse("2026-06-01")));
    assertFalse(ProtocolRules.inForce("active", LocalDate.parse("2026-01-01"), LocalDate.parse("2025-06-01")));
    assertFalse(ProtocolRules.inForce("withdrawn", LocalDate.parse("2026-01-01"), LocalDate.parse("2026-06-01")));
  }
}
