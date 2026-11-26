package eg.gov.health.platform.clinicalforms;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class FormRulesTest {
  @Test void writeOnlyOnActiveTemplatesAndFileOnlySigned() {
    assertTrue(FormRules.instantiable("active"));
    assertFalse(FormRules.instantiable("draft"));
    assertFalse(FormRules.instantiable("retired"));
    assertFalse(FormRules.filable(false));
    assertTrue(FormRules.filable(true));
  }
}
