package eg.gov.health.platform.emergency;
import java.util.*;
/** Nearest-available-unit selection (cycle 224 — referral & medical transport). Distance is haversine;
 *  only units in status 'available' with the required capability qualify.
 *  Deterministic tie-break by unit code keeps dispatch decisions auditable. */
public final class DispatchMath {
  private DispatchMath() {}
  public record UnitView(String unitCode, double lat, double lon, String status, Set<String> capabilities) {}
  public static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
    double r = 6371.0;
    double dLat = Math.toRadians(lat2-lat1), dLon = Math.toRadians(lon2-lon1);
    double a = Math.sin(dLat/2)*Math.sin(dLat/2) + Math.cos(Math.toRadians(lat1))*Math.cos(Math.toRadians(lat2))*Math.sin(dLon/2)*Math.sin(dLon/2);
    return 2 * r * Math.asin(Math.sqrt(a));
  }
  public static Optional<UnitView> nearestAvailable(double lat, double lon, String requiredCapability, List<UnitView> units) {
    return units.stream()
      .filter(u -> "available".equals(u.status()))
      .filter(u -> requiredCapability == null || u.capabilities().contains(requiredCapability))
      .min(Comparator.comparingDouble((UnitView u) -> haversineKm(lat, lon, u.lat(), u.lon())).thenComparing(UnitView::unitCode));
  }
}
