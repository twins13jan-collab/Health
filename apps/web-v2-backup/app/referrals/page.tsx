import ModuleScreen from "@/components/ModuleScreen";

export default function Page() {
  return (
    <ModuleScreen
      config={{
        titleKey: "referrals",
        subtitleAr: "إحالات بين المنشآت بقبول/رفض مسبب من المستقبلة فقط (دورة 224)",
        subtitleEn: "Inter-facility referrals accepted/rejected with reason by the receiver only (cycle 224)",
        endpoint: "/api/referrals",
        envelopeKey: "referrals",
        columns: [
          { key: "reason", ar: "سبب الإحالة", en: "Reason" },
          { key: "urgency", ar: "الإلحاح", en: "Urgency" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
