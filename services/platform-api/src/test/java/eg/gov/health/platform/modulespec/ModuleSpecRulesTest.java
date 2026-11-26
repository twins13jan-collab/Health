package eg.gov.health.platform.modulespec;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class ModuleSpecRulesTest {
  @Test void completenessNeedsEverySection() {
    assertTrue(ModuleSpecRules.missingSections("غرض", "نطاق", "مالك", "أدوار", true, true, true, true, true).isEmpty());
    var m = ModuleSpecRules.missingSections("غرض", "نطاق", "مالك", "أدوار", true, true, false, true, true);
    assertTrue(m.contains("sensitive_fields"));
    assertTrue(ModuleSpecRules.missingSections("", "نطاق", "مالك", "أدوار", true, true, true, true, true).contains("purpose"));
  }
}
