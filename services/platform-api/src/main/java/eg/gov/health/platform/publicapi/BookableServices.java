package eg.gov.health.platform.publicapi;
import java.util.*;
/** The catalog of services a citizen can actually book or request
 *  through the platform, and which facility types offer each. This
 *  is the single source behind the public finder: a service appears
 *  here only if a real fulfilment path exists — clinic sessions are
 *  booked against live session capacity (mode "session"), the rest
 *  are service requests that land in the facility's portal inbox
 *  (mode "request", with the portal request type they file as), and
 *  emergency care is listed as information only (mode "info"):
 *  nobody books an emergency. Facility types not listed for a
 *  service do not offer it — an ambulance station honestly offers
 *  nothing bookable here. */
public final class BookableServices {
  private BookableServices() {}

  public record Bookable(String key, String nameAr, String nameEn, String mode,
                         String portalType, List<String> facilityTypes) {}

  private static final List<String> HOSPITALS = List.of(
      "hospital", "general_hospital", "university_hospital", "emergency_hospital");

  public static final List<Bookable> CATALOG = List.of(
    new Bookable("clinics", "عيادات خارجية — حجز موعد", "Outpatient clinics — book an appointment",
        "session", null,
        List.of("hospital", "general_hospital", "university_hospital", "emergency_hospital",
            "specialized_center", "center", "clinic", "health_center", "health_unit",
            "family_center", "dialysis_center", "oncology_center", "mobile_clinic")),
    new Bookable("lab", "تحاليل معملية", "Laboratory tests",
        "request", "lab",
        List.of("lab", "laboratory", "hospital", "general_hospital", "university_hospital",
            "emergency_hospital", "specialized_center", "center", "clinic", "health_center",
            "family_center", "oncology_center")),
    new Bookable("pharmacy", "طلب أدوية من صيدلية", "Pharmacy medication request",
        "request", "pharmacy",
        List.of("pharmacy", "hospital", "general_hospital", "university_hospital",
            "specialized_center", "oncology_center")),
    new Bookable("radiology", "أشعة وتصوير طبي", "Radiology & imaging",
        "request", "radiology",
        List.of("radiology_center", "hospital", "general_hospital", "university_hospital",
            "emergency_hospital", "specialized_center", "center")),
    new Bookable("dialysis", "جلسات غسيل كلوي", "Dialysis sessions",
        "request", "dialysis",
        List.of("dialysis_center", "hospital", "general_hospital", "university_hospital")),
    new Bookable("home_visit", "زيارة منزلية", "Home visit",
        "request", "home_visit",
        List.of("health_unit", "family_center", "health_center", "clinic", "center",
            "hospital", "general_hospital")),
    new Bookable("vaccination", "تطعيمات", "Vaccinations",
        "request", "other",
        List.of("health_unit", "health_center", "family_center", "mobile_clinic")),
    new Bookable("blood", "تبرع بالدم / طلب دم", "Blood donation / blood request",
        "request", "other",
        List.of("blood_bank", "hospital", "general_hospital", "university_hospital")),
    new Bookable("emergency", "طوارئ — متاحة على مدار الساعة", "Emergency — open around the clock",
        "info", null, HOSPITALS)
  );

  public static Optional<Bookable> byKey(String key) {
    return CATALOG.stream().filter(b -> b.key().equals(key)).findFirst();
  }
  /** The bookable service keys a facility of this type offers. */
  public static List<String> serviceKeysOf(String facilityType) {
    if (facilityType == null) return List.of();
    return CATALOG.stream()
        .filter(b -> b.facilityTypes().contains(facilityType))
        .map(Bookable::key).toList();
  }
  public static boolean offers(String facilityType, String serviceKey) {
    return byKey(serviceKey).map(b -> b.facilityTypes().contains(facilityType)).orElse(false);
  }
}
