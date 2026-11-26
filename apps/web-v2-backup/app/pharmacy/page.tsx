import ModuleScreen from "@/components/ModuleScreen";

export default function Page() {
  return (
    <ModuleScreen
      config={{
        titleKey: "pharmacy",
        subtitleAr: "وصفات بمصالحة دوائية وكشف تفاعلات خطيرة ومنع صرف مزدوج (دورة 222)",
        subtitleEn: "Prescriptions with medication reconciliation, severe-interaction checks and no double dispensing (cycle 222)",
        endpoint: "/api/pharmacy/prescriptions",
        envelopeKey: "prescriptions",
        columns: [
          { key: "drugCode", ar: "رمز الدواء", en: "Drug code" },
          { key: "drugName", ar: "الدواء", en: "Drug" },
          { key: "dose", ar: "الجرعة", en: "Dose" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
