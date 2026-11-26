package eg.gov.health.platform.transparencydeep;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class TransparencyRulesTest {
  @Test void labelledLawfulPublicationAndVisibleCorrection() {
    assertTrue(TransparencyRules.publishBlockers("سجل المنشآت", "منهج العد الموحد", "قانون الإفصاح", false).isEmpty());
    assertTrue(TransparencyRules.publishBlockers("مصدر", "منهج", "أساس", true).contains("contains_sensitive_data"));
    assertTrue(TransparencyRules.publishBlockers("", "منهج", "أساس", false).contains("source_missing"));
    assertFalse(TransparencyRules.correctionValid(" "));
    assertTrue(TransparencyRules.correctionValid("تصحيح: الرقم كان يشمل منشآت تحت الصيانة"));
  }
}
