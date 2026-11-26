package eg.gov.health.platform.envhealth;
/** Environmental-health rules (module 69). Public-health alerts
 *  are only as good as their data: an official alert publishes
 *  only from a RELIABLE feed — a rumour-grade sensor does not get
 *  to frighten a governorate. An emergency-level hazard closes
 *  only with its response plan activated, and an unsafe
 *  drinking-water result closes only when a protective action
 *  was actually taken. */
public final class EnvHealthRules {
  private EnvHealthRules() {}
  public static boolean alertPublishable(boolean feedReliable) { return feedReliable; }
  public static boolean alertClosable(String level, boolean responsePlanActivated) {
    if ("emergency".equals(level)) return responsePlanActivated;
    return true;
  }
  public static boolean waterCheckClosable(String result, String actionTaken) {
    if ("unsafe".equals(result)) return actionTaken != null && !actionTaken.isBlank();
    return true;
  }
}
