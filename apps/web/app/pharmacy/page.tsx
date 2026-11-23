import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
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
        demoRows: [{"id": "d1", "drugCode": "INSULIN", "drugName": "Insulin glargine", "dose": "20 IU", "status": "dispensed"}, {"id": "d2", "drugCode": "WARFARIN", "drugName": "Warfarin", "dose": "5 mg", "status": "prescribed"}],
      }}
    />
  );
}
