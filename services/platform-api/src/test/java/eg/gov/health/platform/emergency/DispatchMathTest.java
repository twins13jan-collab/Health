package eg.gov.health.platform.emergency;
import org.junit.jupiter.api.Test; import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
class DispatchMathTest {
  DispatchMath.UnitView u(String code, double la, double lo, String st, String... caps) {
    return new DispatchMath.UnitView(code, la, lo, st, Set.of(caps));
  }
  @Test void cairoAlexandriaDistance() {
    double d = DispatchMath.haversineKm(30.0444, 31.2357, 31.2001, 29.9187);
    assertTrue(d > 175 && d < 190, "was " + d);
  }
  @Test void nearestAvailableWins() {
    var units = List.of(u("FAR", 30.20, 31.40, "available","ALS"), u("NEAR", 30.05, 31.24, "available","ALS"), u("BUSY", 30.045, 31.236, "dispatched","ALS"));
    var pick = DispatchMath.nearestAvailable(30.0444, 31.2357, "ALS", units).orElseThrow();
    assertEquals("NEAR", pick.unitCode());
  }
  @Test void capabilityFilters() {
    var units = List.of(u("BLS1", 30.05, 31.24, "available","BLS"));
    assertTrue(DispatchMath.nearestAvailable(30.0444, 31.2357, "NEONATAL", units).isEmpty());
  }
}
