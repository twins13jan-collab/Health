import ModuleScreen from "@/components/ModuleScreen";

export default function Page() {
  return (
    <ModuleScreen
      config={{
        titleKey: "clinicalQuality",
        subtitleAr: "مؤشرات نتائج سريرية ووفيات ومضاعفات وإعادة دخول وعدوى مكتسبة + مراجعة وفيات وحالات حرجة واجتماعات متعددة التخصصات + تحليل أسباب جذرية وخطط تحسين وإجراءات تصحيحية ووقائية — والمؤشر لا يقارن إلا معدّلًا باختلاف الحالات، وخطة التحسين لا تغلق قبل التحقق من فاعليتها: الإجراءات المنجزة جهد لا تحسن مثبت",
        subtitleEn: "Clinical outcome, mortality, complication, readmission and hospital-acquired-infection indicators + mortality and critical-case reviews and multidisciplinary meetings + root-cause analysis, improvement plans and corrective and preventive actions — an indicator compares only case-mix adjusted, and an improvement plan closes only when its effectiveness is verified: actions completed is effort, not proven improvement",
        endpoint: "/api/clinical-quality/indicators",
        envelopeKey: "indicators",
        columns: [
          { key: "name", ar: "المؤشر", en: "Indicator" },
          { key: "category", ar: "الفئة", en: "Category" },
          { key: "value", ar: "القيمة", en: "Value" },
        ],
      }}
    />
  );
}
