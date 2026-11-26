package eg.gov.health.platform.partnerships;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
class PartnershipRulesTest {
  @Test void contractNeedsPrivacySecurityScopePurposeAndValidDates() {
    var s = LocalDate.parse("2026-01-01"); var e = LocalDate.parse("2027-01-01");
    assertTrue(PartnershipRules.contractActivatable(true, true, "شبكة داخلية للقراءة", "صيانة الأجهزة", s, e));
    assertFalse(PartnershipRules.contractActivatable(false, true, "x", "y", s, e)); // no privacy terms
    assertFalse(PartnershipRules.contractActivatable(true, true, " ", "y", s, e));  // no access scope
    assertFalse(PartnershipRules.contractActivatable(true, true, "x", "y", e, s));  // inverted dates
  }
  @Test void grantsNeedActivePartnerContractAndFutureExpiry() {
    var now = OffsetDateTime.parse("2026-11-22T10:00:00+02:00");
    assertTrue(PartnershipRules.grantPermitted("active", "active", now.plusMonths(6), now));
    assertFalse(PartnershipRules.grantPermitted("terminated", "active", now.plusMonths(6), now));
    assertFalse(PartnershipRules.grantPermitted("active", "draft", now.plusMonths(6), now));
    assertFalse(PartnershipRules.grantPermitted("active", "active", now.minusDays(1), now));
  }
  @Test void weakPerformanceIsFlagged() {
    assertTrue(PartnershipRules.reviewFlagsAction(72.0, 0));
    assertTrue(PartnershipRules.reviewFlagsAction(95.0, 3));
    assertFalse(PartnershipRules.reviewFlagsAction(95.0, 1));
  }
}
