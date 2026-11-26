package eg.gov.health.platform.diagnostics;
import java.math.BigDecimal; import java.util.Map;
/** Critical (panic) value rules — cycle 219: a critical result creates an
 *  alert that cannot be closed without a documented acknowledgement or a
 *  timed escalation to the named alternate. Limits are facility-configurable
 *  in production; these are the national default reference limits. */
public final class CriticalValueRules {
  private CriticalValueRules() {}
  public record Limit(BigDecimal low, BigDecimal high) {}
  public static final Map<String, Limit> LIMITS = Map.of(
    "K",   new Limit(new BigDecimal("2.8"), new BigDecimal("6.0")),
    "NA",  new Limit(new BigDecimal("120"), new BigDecimal("160")),
    "GLU", new Limit(new BigDecimal("40"),  new BigDecimal("500")),
    "HB",  new Limit(new BigDecimal("7.0"), new BigDecimal("20.0")),
    "PLT", new Limit(new BigDecimal("50"),  new BigDecimal("1000")),
    "CA",  new Limit(new BigDecimal("6.5"), new BigDecimal("13.0")),
    "CREA",new Limit(null,                   new BigDecimal("4.0")));
  public static boolean isCritical(String testCode, BigDecimal value) {
    var lim = LIMITS.get(testCode == null ? "" : testCode.toUpperCase());
    if (lim == null || value == null) return false;
    if (lim.low() != null && value.compareTo(lim.low()) < 0) return true;
    return lim.high() != null && value.compareTo(lim.high()) > 0;
  }
}
