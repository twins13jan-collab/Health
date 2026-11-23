import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "nutrition",
        subtitleAr: "فصل صارم بين الموصوف والمنفذ فعليًا: الفجوة تظهر بسببها ولا تُعاد كتابة السجل + اختصاصي التغذية يقترح والطبيب يعتمد والصيدلي يراجع الوريدي قبل بدئه + لا استبدال تلقائي لمكون غير متاح",
        subtitleEn: "A strict split between prescribed and actually given: the gap is shown with its reason and the record is never rewritten + the dietitian proposes, the physician approves, the pharmacist reviews parenteral orders before they start + no automatic substitution of an unavailable component",
        endpoint: "/api/nutrition/plans?patientId=00000000-0000-0000-0000-000000000000",
        envelopeKey: "plans",
        columns: [
          { key: "route", ar: "الطريق", en: "Route", status: true },
          { key: "estimatedNeedsKcal", ar: "الاحتياج (سعرة)", en: "Needs (kcal)" },
          { key: "goal", ar: "الهدف", en: "Goal" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
        demoRows: [{ "id": "n1", "route": "enteral", "estimatedNeedsKcal": 1900, "goal": "دعم التئام", "status": "active" }],
      }}
    />
  );
}
