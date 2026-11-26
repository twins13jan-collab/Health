import ModuleScreen from "@/components/ModuleScreen";

export default function Page() {
  return (
    <ModuleScreen
      config={{
        titleKey: "proms",
        subtitleAr: "أدوات قياس معتمدة ونسخ لغوية معتمدة فقط تُجدول في توقيتات ثابتة بمسار الرعاية + إشارة الاحتياج العاجل في أي رد حدث سريري يمنع إغلاق الجدولة حتى يُتابَع + التحليل يحترم حجم العينة: دون 30 تصف الأرقام المجيبين لا الخدمة ولا تُنشر كاستنتاجات + النتائج تكمّل التقييم السريري ولا تستبدله",
        subtitleEn: "Only validated instruments in validated language versions are scheduled, at fixed points in the care pathway + an urgent-need signal in any response is a clinical event that blocks schedule closure until followed up + analysis respects sample size: below 30 the figures describe respondents, not the service, and are not published as conclusions + results complement clinical assessment, never replace it",
        endpoint: "/api/proms/instruments",
        envelopeKey: "instruments",
        columns: [
          { key: "name", ar: "الأداة", en: "Instrument" },
          { key: "language", ar: "اللغة", en: "Language" },
          { key: "domain", ar: "المجال", en: "Domain" },
        ],
      }}
    />
  );
}
