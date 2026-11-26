package eg.gov.health.platform.quality;
import java.util.*;
/** Infection-control precaution mapping: condition -> required precaution
 *  level (standard / contact / droplet / airborne). Airborne conditions
 *  additionally require a negative-pressure room flag for bed assignment. */
public final class IsolationPrecautions {
  private IsolationPrecautions() {}
  static final Map<String,String> MAP = Map.ofEntries(
    Map.entry("MRSA","contact"), Map.entry("C_DIFF","contact"), Map.entry("VRE","contact"),
    Map.entry("INFLUENZA","droplet"), Map.entry("MENINGOCOCCAL","droplet"), Map.entry("COVID19","droplet"),
    Map.entry("TB","airborne"), Map.entry("MEASLES","airborne"), Map.entry("VARICELLA","airborne"));
  public static String precautionFor(String conditionCode) {
    if (conditionCode == null) return "standard";
    return MAP.getOrDefault(conditionCode.toUpperCase(), "standard");
  }
  public static boolean requiresNegativePressure(String conditionCode) { return "airborne".equals(precautionFor(conditionCode)); }
}
