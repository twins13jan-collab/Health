package eg.gov.health.platform.geodata;
import java.math.*; import java.time.*;
/** Geographic data rules (module 121). A facility location counts
 *  only with documented coordinates inside the national bounds, a
 *  recorded accuracy and a verification date — and a verification
 *  older than a year is stale, shown as such. Access analysis runs
 *  on a trusted road network: the straight-line distance is NEVER
 *  presented as travel time, and an analysis without a road-network
 *  time is incomplete by definition. */
public final class GeoRules {
  private GeoRules() {}
  public static boolean coordinatesValid(BigDecimal lat, BigDecimal lng) {
    if (lat == null || lng == null) return false;
    double la = lat.doubleValue(), lo = lng.doubleValue();
    return la >= 22.0 && la <= 31.9 && lo >= 24.7 && lo <= 37.0; // Egypt bounds
  }
  public static boolean verificationStale(LocalDate verifiedAt, LocalDate today) {
    return verifiedAt == null || (today != null && verifiedAt.plusMonths(12).isBefore(today));
  }
  public static boolean analysisComplete(Integer roadTimeMin, String roadNetworkSource) {
    return roadTimeMin != null && roadNetworkSource != null && !roadNetworkSource.isBlank();
  }
  /** The one number that must never be served as a travel time. */
  public static String presentationNote(BigDecimal straightLineKm, Integer roadTimeMin) {
    if (roadTimeMin == null) return "straight-line distance " + straightLineKm + " km is NOT a travel time — road-network analysis required";
    return "travel time from the road network: " + roadTimeMin + " min (straight-line " + straightLineKm + " km shown for reference only)";
  }
}
