package eg.gov.health.platform.servicecatalog;
import org.junit.jupiter.api.Test;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
class CatalogRulesTest {
  @Test void activationAndWindows() {
    assertTrue(CatalogRules.activatable("طبيب + جهاز أشعة", "مركز الأشعة", LocalDate.parse("2026-12-01")));
    assertFalse(CatalogRules.activatable("", "مركز الأشعة", LocalDate.parse("2026-12-01")));
    assertFalse(CatalogRules.activatable("طبيب", null, LocalDate.parse("2026-12-01")));
    assertTrue(CatalogRules.applicable("active", LocalDate.parse("2026-12-01"), null, LocalDate.parse("2026-12-15")));
    assertFalse(CatalogRules.applicable("active", LocalDate.parse("2026-12-01"), null, LocalDate.parse("2026-11-15")));
    assertFalse(CatalogRules.applicable("draft", LocalDate.parse("2026-12-01"), null, LocalDate.parse("2026-12-15")));
  }
}
