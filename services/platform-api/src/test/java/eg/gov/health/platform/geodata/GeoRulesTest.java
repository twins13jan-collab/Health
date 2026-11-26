package eg.gov.health.platform.geodata;
import org.junit.jupiter.api.Test;
import java.math.*; import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
class GeoRulesTest {
  @Test void coordinatesMustSitInsideNationalBounds() {
    assertTrue(GeoRules.coordinatesValid(new BigDecimal("30.0444"), new BigDecimal("31.2357"))); // Cairo
    assertFalse(GeoRules.coordinatesValid(new BigDecimal("48.85"), new BigDecimal("2.35")));     // Paris
    assertFalse(GeoRules.coordinatesValid(null, new BigDecimal("31.0")));
  }
  @Test void verificationOlderThanAYearIsStale() {
    assertTrue(GeoRules.verificationStale(LocalDate.parse("2024-01-01"), LocalDate.parse("2026-11-22")));
    assertFalse(GeoRules.verificationStale(LocalDate.parse("2026-06-01"), LocalDate.parse("2026-11-22")));
    assertTrue(GeoRules.verificationStale(null, LocalDate.parse("2026-11-22")));
  }
  @Test void straightLineIsNeverATravelTime() {
    assertFalse(GeoRules.analysisComplete(null, "شبكة الطرق الوطنية"));
    assertTrue(GeoRules.analysisComplete(35, "شبكة الطرق الوطنية"));
    assertTrue(GeoRules.presentationNote(new BigDecimal("12.5"), null).contains("NOT a travel time"));
    assertTrue(GeoRules.presentationNote(new BigDecimal("12.5"), 35).contains("35 min"));
  }
}
