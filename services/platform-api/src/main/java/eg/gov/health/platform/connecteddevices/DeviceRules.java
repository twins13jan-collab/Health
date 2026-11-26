package eg.gov.health.platform.connecteddevices;
import java.time.*;
/** Connected-device rules (module 128). A reading is reliable
 *  only from a device in calibration with good signal — a
 *  reading from an overdue device is recorded, flagged
 *  unreliable, and never fires an alert. Physiologically
 *  impossible values are anomalies, quarantined from the trend
 *  with their raw value and provenance preserved: the record
 *  keeps everything, but only trustworthy readings steer care. */
public final class DeviceRules {
  private DeviceRules() {}
  public static boolean calibrationValid(LocalDate calibrationDue, LocalDate day) {
    return calibrationDue != null && day != null && !day.isAfter(calibrationDue);
  }
  public static boolean plausible(String metric, double value) {
    return switch (metric) {
      case "heart_rate" -> value >= 20 && value <= 300;
      case "spo2" -> value >= 40 && value <= 100;
      case "temperature" -> value >= 25 && value <= 45;
      default -> true;
    };
  }
  public static boolean reliable(boolean calibrationValid, String signalQuality, boolean plausible) {
    return calibrationValid && "good".equals(signalQuality) && plausible;
  }
}
