package eg.gov.health.platform.cssd;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
class CssdRulesTest {
  @Test void chainMustBeComplete() {
    assertTrue(CssdRules.chainComplete(true, true, true, true, "فني تعقيم"));
    assertFalse(CssdRules.chainComplete(true, false, true, true, "فني تعقيم")); // disinfection skipped
    assertFalse(CssdRules.chainComplete(true, true, true, true, null)); // packer undocumented
  }
  @Test void releaseNeedsIndicatorsAndImplantsWaitForBiological() {
    assertTrue(CssdRules.releasable(true, "pass", "pending", false));
    assertFalse(CssdRules.releasable(true, "pass", "pending", true)); // implant load: pending is not a pass
    assertTrue(CssdRules.releasable(true, "pass", "pass", true));
    assertFalse(CssdRules.releasable(true, "fail", "pass", false));
    assertFalse(CssdRules.releasable(false, "pass", "pass", false));
    assertFalse(CssdRules.releasable(true, "pass", "fail", false));
  }
  @Test void expiredSetsAreNeverIssued() {
    var today = LocalDate.of(2026, 11, 22);
    assertTrue(CssdRules.issuable("sterile", LocalDate.of(2026, 12, 1), today));
    assertFalse(CssdRules.issuable("sterile", LocalDate.of(2026, 11, 1), today));
    assertFalse(CssdRules.issuable("quarantined", LocalDate.of(2026, 12, 1), today));
  }
}
