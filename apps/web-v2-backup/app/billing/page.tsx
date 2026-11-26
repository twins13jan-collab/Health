import ModuleScreen from "@/components/ModuleScreen";

export default function Page() {
  return (
    <ModuleScreen
      config={{
        titleKey: "billing",
        subtitleAr: "فواتير تُحسب خادميًا وتُقفل بعد الإصدار ومسار مطالبات مغلق (دورة 228)",
        subtitleEn: "Server-computed invoices, locked after issue, closed claims path (cycle 228)",
        endpoint: "/api/billing/invoices",
        envelopeKey: "invoices",
        columns: [
          { key: "payer", ar: "الجهة الدافعة", en: "Payer" },
          { key: "totalAmount", ar: "الإجمالي", en: "Total" },
          { key: "insurerAmount", ar: "حصة المؤمّن", en: "Insurer" },
          { key: "patientAmount", ar: "حصة المريض", en: "Patient" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
