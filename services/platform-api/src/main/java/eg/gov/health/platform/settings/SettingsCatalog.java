package eg.gov.health.platform.settings;
import java.util.*;
/** The catalogue of platform settings the admin panel may control.
 *  Every key is typed and validated here, and every key is wired to
 *  a real runtime consumer (login lockout, password policy,
 *  registration switches, maintenance mode, the public website) —
 *  a key with no consumer does not belong in this catalogue.
 *  Pure logic, unit-tested. */
public final class SettingsCatalog {
  private SettingsCatalog() {}
  public record Spec(String key, String type, String group, boolean isPublic, Integer min, Integer max) {}
  private static final Map<String, Spec> SPECS = new LinkedHashMap<>();
  private static void add(String key, String type, String group, boolean isPublic) {
    SPECS.put(key, new Spec(key, type, group, isPublic, null, null));
  }
  private static void addInt(String key, String group, int min, int max) {
    SPECS.put(key, new Spec(key, "int", group, false, min, max));
  }
  static {
    add("site.name_ar", "string", "identity", true);
    add("site.name_en", "string", "identity", true);
    add("site.descriptor", "string", "identity", true);
    add("site.contact_name", "string", "contact", true);
    add("site.contact_phone", "string", "contact", true);
    add("site.contact_whatsapp", "string", "contact", true);
    addInt("security.lockout_max_failures", "security", 1, 20);
    addInt("security.lockout_minutes", "security", 1, 1440);
    addInt("security.password_min_length", "security", 8, 64);
    add("registration.patient_enabled", "bool", "registration", false);
    add("registration.facility_enabled", "bool", "registration", false);
    add("system.maintenance_mode", "bool", "system", true);
    add("system.maintenance_message", "string", "system", true);
    add("public.show_capacity", "bool", "public", true);
  }
  public static boolean known(String key) { return SPECS.containsKey(key); }
  public static Spec spec(String key) { return SPECS.get(key); }
  public static Collection<Spec> all() { return SPECS.values(); }
  public static boolean isPublic(String key) { var s = SPECS.get(key); return s != null && s.isPublic(); }
  /** Returns an Arabic error message, or null when the value is valid. */
  public static String validate(String key, String value) {
    var s = SPECS.get(key);
    if (s == null) return "إعداد غير معروف: " + key;
    if (value == null) return "القيمة مطلوبة";
    switch (s.type()) {
      case "bool":
        if (!value.equals("true") && !value.equals("false")) return "القيمة يجب أن تكون true أو false";
        return null;
      case "int":
        try {
          int n = Integer.parseInt(value.trim());
          if (s.min() != null && n < s.min()) return "القيمة أقل من الحد الأدنى " + s.min();
          if (s.max() != null && n > s.max()) return "القيمة أكبر من الحد الأقصى " + s.max();
          return null;
        } catch (NumberFormatException e) { return "القيمة يجب أن تكون رقمًا صحيحًا"; }
      default:
        if (value.isBlank()) return "القيمة لا يمكن أن تكون فارغة";
        if (value.length() > 500) return "القيمة أطول من اللازم";
        return null;
    }
  }
}
