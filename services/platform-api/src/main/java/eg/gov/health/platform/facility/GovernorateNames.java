package eg.gov.health.platform.facility;
import java.util.*;
/** The 27 governorates in their three written forms (code, Arabic
 *  name, English name) exactly as loaded in ref_governorates.
 *  Records across the platform store the governorate in any of the
 *  three forms (seed data uses Arabic names, newer registrations
 *  use codes), so scope checks must treat the forms as equivalent
 *  instead of comparing raw strings. */
public final class GovernorateNames {
  private GovernorateNames() {}
  public record Gov(String code, String nameAr, String nameEn) {}
  public static final List<Gov> ALL = List.of(
      new Gov("ALX", "الإسكندرية", "Alexandria"),
      new Gov("ASW", "أسوان", "Aswan"),
      new Gov("ASY", "أسيوط", "Asyut"),
      new Gov("BEH", "البحيرة", "Beheira"),
      new Gov("BNS", "بني سويف", "Beni Suef"),
      new Gov("CAI", "القاهرة", "Cairo"),
      new Gov("DAK", "الدقهلية", "Dakahlia"),
      new Gov("DAM", "دمياط", "Damietta"),
      new Gov("FYM", "الفيوم", "Faiyum"),
      new Gov("GHA", "الغربية", "Gharbia"),
      new Gov("GIZ", "الجيزة", "Giza"),
      new Gov("ISM", "الإسماعيلية", "Ismailia"),
      new Gov("KFS", "كفر الشيخ", "Kafr El Sheikh"),
      new Gov("LUX", "الأقصر", "Luxor"),
      new Gov("MAT", "مطروح", "Matruh"),
      new Gov("MIN", "المنيا", "Minya"),
      new Gov("MON", "المنوفية", "Monufia"),
      new Gov("POR", "بورسعيد", "Port Said"),
      new Gov("QAL", "القليوبية", "Qalyubia"),
      new Gov("QEN", "قنا", "Qena"),
      new Gov("RED", "البحر الأحمر", "Red Sea"),
      new Gov("SHR", "الشرقية", "Sharqia"),
      new Gov("SIN_N", "شمال سيناء", "North Sinai"),
      new Gov("SIN_S", "جنوب سيناء", "South Sinai"),
      new Gov("SOH", "سوهاج", "Sohag"),
      new Gov("SUZ", "السويس", "Suez"),
      new Gov("WAD", "الوادي الجديد", "New Valley"));

  private static String norm(String s) { return s == null ? "" : s.trim(); }
  /** The canonical row for any written form, or null. */
  public static Gov of(String anyForm) {
    String v = norm(anyForm);
    if (v.isEmpty()) return null;
    for (var g : ALL) {
      if (g.code().equalsIgnoreCase(v) || g.nameAr().equals(v) || g.nameEn().equalsIgnoreCase(v)) return g;
    }
    return null;
  }
  /** True when both values denote the same governorate, in any forms. */
  public static boolean matches(String a, String b) {
    if (a == null || b == null) return false;
    if (norm(a).equalsIgnoreCase(norm(b))) return true;
    var ga = of(a); var gb = of(b);
    return ga != null && ga == gb;
  }
  /** The health directorate's name for a governorate in any form. */
  public static String directorateAr(String anyForm) {
    var g = of(anyForm);
    return g == null ? null : "مديرية صحة " + g.nameAr();
  }
}
