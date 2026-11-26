package eg.gov.health.platform.reports;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
class ReportRulesTest {
  @Test void coverageAndClass() {
    assertEquals(60.0, ReportRules.coveragePct(3, 5));
    assertEquals(100.0, ReportRules.coveragePct(5, 5));
    assertEquals("incomplete", ReportRules.dataClass(60.0, false));
    assertEquals("confirmed", ReportRules.dataClass(100.0, false));
    assertEquals("late", ReportRules.dataClass(90.0, true));
    assertFalse(ReportRules.nationalClaimAllowed(60.0)); // never generalised nationally
    assertTrue(ReportRules.nationalClaimAllowed(85.0));
  }
  @Test void smallCountsSuppressed() {
    assertTrue(ReportRules.smallCountSuppressed(3));
    assertFalse(ReportRules.smallCountSuppressed(5));
    assertFalse(ReportRules.smallCountSuppressed(0));
  }
  @Test void versionComparisonWarns() {
    assertTrue(ReportRules.comparabilityWarningRequired(1, 2));
    assertFalse(ReportRules.comparabilityWarningRequired(2, 2));
  }
  @Test void publishableGateAndRecompute() {
    assertFalse(ReportRules.publishable("draft", "validated", true, "الجهة القومية"));
    assertFalse(ReportRules.publishable("approved", "validated", false, "الجهة القومية"));
    assertFalse(ReportRules.publishable("approved", "computed", true, "الجهة القومية"));
    assertTrue(ReportRules.publishable("approved", "validated", true, "الجهة القومية"));
    assertEquals(new BigDecimal("0.0500"), ReportRules.compute(new BigDecimal("45"), new BigDecimal("900")));
    assertNull(ReportRules.compute(new BigDecimal("1"), BigDecimal.ZERO));
  }
}
