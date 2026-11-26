package eg.gov.health.platform.auditcompliance;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ComplianceRulesTest {
  @Test void verifiedClosureAndNoAutomaticJudgment() {
    assertFalse(ComplianceRules.findingClosable("دليل-1", "إجراء", false));
    assertFalse(ComplianceRules.findingClosable("", "إجراء", true));
    assertTrue(ComplianceRules.findingClosable("دليل-1", "إجراء", true));
    assertEquals("signal_only", ComplianceRules.signalStatus(false, null));
    assertEquals("signal_only", ComplianceRules.signalStatus(true, " "));
    assertEquals("human_decided", ComplianceRules.signalStatus(true, "إحالة للتحقيق"));
  }
}
