package eg.gov.health.platform.governingrules;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class GoverningRulesTest {
  @Test void complianceNeedsEvidenceAndIndependence() {
    assertEquals("verified", GoverningRules.verdict(true, "تقرير اختبار اختراق 2026", true));
    assertEquals("self_declared", GoverningRules.verdict(true, "تقرير داخلي", false));
    assertEquals("unverified", GoverningRules.verdict(true, "", true));
    assertEquals("non_compliant", GoverningRules.verdict(false, "تقرير", true));
    assertTrue(GoverningRules.scopeReady(9, 9));
    assertFalse(GoverningRules.scopeReady(9, 8));
    assertFalse(GoverningRules.scopeReady(0, 0));
  }
}
