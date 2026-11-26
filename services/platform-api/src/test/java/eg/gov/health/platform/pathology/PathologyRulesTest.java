package eg.gov.health.platform.pathology;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class PathologyRulesTest {
  @Test void unverifiedSpecimenQuarantined() {
    assertEquals("quarantined", PathologyRules.receiptStatus(false));
    assertEquals("received", PathologyRules.receiptStatus(true));
    assertFalse(PathologyRules.processable("quarantined"));
    assertFalse(PathologyRules.processable("received"));
    assertTrue(PathologyRules.processable("accepted"));
  }
  @Test void insufficiencyNeedsNote() {
    assertFalse(PathologyRules.insufficiencyDocumented(" "));
    assertTrue(PathologyRules.insufficiencyDocumented("نسيج غير كافٍ للتشخيص"));
  }
  @Test void finalReportNeedsSignature() {
    assertFalse(PathologyRules.finalReportValid("final", false, "د. باثولوجي"));
    assertFalse(PathologyRules.finalReportValid("final", true, " "));
    assertTrue(PathologyRules.finalReportValid("final", true, "د. باثولوجي"));
    assertTrue(PathologyRules.finalReportValid("preliminary", false, null));
  }
  @Test void amendmentNeedsReason() {
    assertFalse(PathologyRules.amendmentValid(null));
    assertTrue(PathologyRules.amendmentValid("تصحيح تصنيف بعد صبغة إضافية"));
  }
}
