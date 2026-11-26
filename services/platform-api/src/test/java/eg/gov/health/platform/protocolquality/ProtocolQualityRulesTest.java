package eg.gov.health.platform.protocolquality;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
class ProtocolQualityRulesTest {
  @Test void onlyCurrentInDateProtocolsBind() {
    var today = LocalDate.parse("2026-11-22");
    assertTrue(ProtocolQualityRules.applicable("current", LocalDate.parse("2027-03-01"), today));
    assertFalse(ProtocolQualityRules.applicable("withdrawn", LocalDate.parse("2027-03-01"), today));
    assertFalse(ProtocolQualityRules.applicable("current", LocalDate.parse("2026-06-01"), today)); // stale
    assertEquals(25.0, ProtocolQualityRules.unjustifiedRatePct(40, 10));
    assertEquals(85.0, ProtocolQualityRules.adherencePct(200, 170));
  }
}
