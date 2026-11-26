import ModuleScreen from "@/components/ModuleScreen";

export default function Page() {
  return (
    <ModuleScreen
      config={{
        titleKey: "stroke",
        subtitleAr: "مسار سكتة بساعات موثقة: آخر حالة طبيعية والوصول والأشعة والقرار والإبرة + الإذابة فقط بعد استبعاد النزف بالأشعة وقائمة موانع نظيفة وداخل نافذة 4.5 ساعة + النقل العاجل بقبول موثق + لا تناول بالفم قبل فحص بلع ناجح",
        subtitleEn: "A stroke pathway on documented clocks: last known well, arrival, CT, decision and needle + thrombolysis only after CT excludes hemorrhage, a clean contraindication checklist and inside the 4.5-hour window + urgent transfer only with documented acceptance + no oral intake before a passed swallow screen",
        endpoint: "/api/stroke/activations",
        envelopeKey: "activations",
        columns: [
          { key: "patientRef", ar: "المريض", en: "Patient" },
          { key: "nihss", ar: "NIHSS", en: "NIHSS" },
          { key: "ctResult", ar: "الأشعة", en: "CT" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
