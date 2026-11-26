package eg.gov.health.platform.apimanagement;
/** API-management rules (module 125). A breaking version never
 *  silently replaces a live one: the old version enters a
 *  deprecation window first, so consumers migrate instead of
 *  breaking at 3 a.m. A revoked key is dead immediately, and a
 *  consumer past quota is throttled — quotas are contracts, not
 *  suggestions. */
public final class ApiRules {
  private ApiRules() {}
  public static boolean versionPublishable(boolean backwardCompatible, boolean previousDeprecated) {
    return backwardCompatible || previousDeprecated;
  }
  public static boolean consumerAllowed(String keyStatus, Integer usageToday, Integer quotaPerDay) {
    if (!"active".equals(keyStatus)) return false;
    return quotaPerDay == null || usageToday == null || usageToday < quotaPerDay;
  }
}
