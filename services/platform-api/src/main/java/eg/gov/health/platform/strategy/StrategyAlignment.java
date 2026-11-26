package eg.gov.health.platform.strategy;
import java.util.*;
/** How the platform serves each priority of the National Health
 *  Strategy 2024–2030: the live operational capabilities (screens
 *  and workflows) that implement it, and the indicator keys the
 *  strategy board measures from real platform data. A capability
 *  is listed only when the platform actually runs it; an
 *  indicator is shown only when the platform truly measures it —
 *  the strategy's own national targets live in the strategy's
 *  executive plan and are not invented here. */
public final class StrategyAlignment {
  private StrategyAlignment() {}
  public record Capability(String labelAr, String labelEn, String path) {}
  public record Indicator(String key, String labelAr, String labelEn, String unit) {}

  public static final Map<String, List<Capability>> CAPABILITIES = Map.of(
    "P1", List.of(
      new Capability("مطالبات التأمين الصحي والفصل والتسوية", "Insurance claims & settlement", "/insurance-deep"),
      new Capability("الفوترة والحماية من المخاطر المالية", "Billing & financial protection", "/billing"),
      new Capability("العيادات الخارجية والحجز", "Outpatient clinics & booking", "/clinics"),
      new Capability("إدارة القطاع الخاص والتراخيص", "Private-sector management & licensing", "/admin/private-sector"),
      new Capability("دليل المنشآت والسعة", "Facility directory & capacity", "/facilities")),
    "P2", List.of(
      new Capability("الأمومة والولادة", "Maternity & delivery", "/maternity"),
      new Capability("حديثو الولادة والحضانات", "Neonatal & NICU", "/neonatal"),
      new Capability("التغذية العلاجية", "Clinical nutrition", "/nutrition"),
      new Capability("التأهيل", "Rehabilitation", "/rehab")),
    "P3", List.of(
      new Capability("الصحة النفسية", "Mental health", "/mental-health"),
      new Capability("السجلات الطبية والترميز ICD-10", "Medical records & ICD-10 coding", "/medical-records"),
      new Capability("التحليلات والتنبؤ", "Analytics & forecasting", "/analytics"),
      new Capability("العيادات وبرامج المتابعة", "Clinics & follow-up programmes", "/clinics")),
    "P4", List.of(
      new Capability("الطوارئ والفرز", "Emergency & triage", "/er"),
      new Capability("لوحة القيادة والسعة اللحظية", "Live command dashboard & capacity", "/dashboard"),
      new Capability("التحليلات: التفشي ومقاومة المضادات", "Analytics: outbreaks & AMR", "/analytics"),
      new Capability("التعقيم المركزي وسلامة المنشآت", "CSSD & facility safety", "/cssd")),
    "P5", List.of(
      new Capability("مصفوفة الصلاحيات والأدوار", "Role & access matrix", "/admin/access-matrix"),
      new Capability("إعدادات ومتغيرات المنظومة", "Platform settings & variables", "/admin/settings"),
      new Capability("إدارة الحسابات والمنشآت والاعتمادات", "Accounts, facilities & approvals", "/admin"),
      new Capability("حوكمة دورات العمل", "Workflow governance", "/cycles")),
    "P6", List.of(
      new Capability("مساعد خطاب الذكي", "Khattab AI assistant", "/assistant"),
      new Capability("بوابة المريض الرقمية", "Digital patient portal", "/my"),
      new Capability("العمل دون اتصال", "Offline-capable work", "/offline"),
      new Capability("تطبيق أندرويد للميدان", "Android field app", "/download-app")),
    "P7", List.of(
      new Capability("ابحث واحجز خدمة (الموقع العام)", "Public find & book a service", "/services"),
      new Capability("حسابي الصحي", "My health account", "/my"),
      new Capability("شكاوى المرضى ومتابعتها", "Patient complaints & follow-up", "/portal-inbox")));

  public static final Map<String, List<Indicator>> INDICATORS = Map.of(
    "P1", List.of(
      new Indicator("facilities_active", "منشآت نشطة", "Active facilities", "count"),
      new Indicator("governorates_covered", "محافظات مخدومة بمنشأة نشطة", "Governorates with an active facility", "/27"),
      new Indicator("phc_facilities", "منشآت رعاية أولية (وحدة/مركز صحة/صحة أسرة)", "Primary-care facilities", "count"),
      new Indicator("private_facilities", "منشآت قطاع خاص مسجلة", "Registered private-sector facilities", "count"),
      new Indicator("claims_settled_pct", "مطالبات تأمين مسوّاة", "Insurance claims settled", "%"),
      new Indicator("patient_share_pct", "حصة المريض من الفواتير المُصدرة (مؤشر حماية مالية)", "Patient share of issued invoices (financial-protection proxy)", "%")),
    "P2", List.of(
      new Indicator("vaccinations_count", "تطعيمات مسجلة", "Recorded vaccinations", "count"),
      new Indicator("neonatal_screenings_count", "فحوص حديثي ولادة", "Newborn screenings", "count"),
      new Indicator("clinic_visits_count", "زيارات عيادات مسجلة", "Recorded clinic visits", "count")),
    "P3", List.of(
      new Indicator("patients_registered", "مرضى مسجلون بالسجل الوطني", "Patients in the national record", "count"),
      new Indicator("encounters_total", "لقاءات سريرية موثقة", "Documented clinical encounters", "count"),
      new Indicator("encounters_today", "لقاءات اليوم", "Encounters today", "count")),
    "P4", List.of(
      new Indicator("ambulances_available", "سيارات إسعاف متاحة الآن", "Ambulances available now", "count"),
      new Indicator("er_open_now", "حالات طوارئ مفتوحة الآن", "Open emergency cases now", "count"),
      new Indicator("disaster_events_count", "أحداث كوارث وطوارئ كبرى مسجلة", "Recorded disaster/major events", "count")),
    "P5", List.of(
      new Indicator("audit_events_count", "أحداث مدققة بالسجل (شفافية)", "Audited events on record (transparency)", "count"),
      new Indicator("matrix_domains", "نطاقات صلاحيات محكومة بالمصفوفة", "Access-matrix governed domains", "count"),
      new Indicator("facilities_licensed_pct", "منشآت بترخيص سارٍ من المسجلة تراخيصها", "Facilities with a valid licence (of those recorded)", "%")),
    "P6", List.of(
      new Indicator("portal_patients", "مواطنون مسجلون بالبوابة الرقمية", "Citizens registered on the digital portal", "count"),
      new Indicator("portal_requests", "طلبات خدمة رقمية مقدمة", "Digital service requests filed", "count"),
      new Indicator("clinic_sessions_today", "جلسات عيادات اليوم", "Clinic sessions today", "count")),
    "P7", List.of(
      new Indicator("complaints_total", "شكاوى مواطنين مسجلة", "Citizen complaints recorded", "count"),
      new Indicator("complaints_resolved_pct", "شكاوى مغلقة/محسومة", "Complaints resolved/closed", "%")));
}
