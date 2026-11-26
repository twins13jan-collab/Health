import ModuleScreen from "@/components/ModuleScreen";

export default function Page() {
  return (
    <ModuleScreen
      config={{
        titleKey: "followUp",
        subtitleAr: "خطة متابعة عند الخروج بمسؤول مسمى ومهام مؤرخة + مهمة متأخرة تصعد ولا تسقط بصمت + نتائج وصلت بعد الخروج تراجع ويُبلَّغ المريض + فحوص الأدوية عالية الخطورة جزء من الخطة + لا إغلاق وبنود مصعدة أو غير مراجعة مفتوحة",
        subtitleEn: "A discharge follow-up plan with a named owner and dated tasks + an overdue task escalates, never silently drops + results arriving after discharge are reviewed and the patient notified + high-risk medication checks are part of the plan + no closure with escalated or unreviewed items open",
        endpoint: "/api/follow-up/plans",
        envelopeKey: "plans",
        columns: [
          { key: "patientRef", ar: "المريض", en: "Patient" },
          { key: "responsible", ar: "المسؤول", en: "Owner" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
