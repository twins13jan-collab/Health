package eg.gov.health.platform.mobiledevices;
/** Mobile-device rules (module 127). A device is an identity:
 *  bound to a user and a facility, encrypted, and current. An
 *  unencrypted device holds no clinical session; an app below
 *  the minimum supported version must update before it works;
 *  and a lost device or an ended authorization ends in a wipe —
 *  for a stated reason, never casually. */
public final class MobileRules {
  private MobileRules() {}
  public static boolean sessionAllowed(boolean encryptionEnabled, String status) {
    return encryptionEnabled && "active".equals(status);
  }
  public static boolean updateRequired(Integer appVersion, Integer minSupported) {
    return appVersion != null && minSupported != null && appVersion < minSupported;
  }
  public static boolean wipeValid(String reason) {
    return "lost".equals(reason) || "stolen".equals(reason) || "authorization_ended".equals(reason);
  }
}
