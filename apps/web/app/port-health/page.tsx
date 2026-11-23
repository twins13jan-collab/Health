import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "portHealth",
        subtitleAr: "بلاغات صحية للمسافرين وتقييم مخاطر وفحوص وعزل وإحالة بالمنافذ + عالي الخطورة بأعراض يُعزل أو يُحال ولا يُخلى سبيله أبدًا + الحجر بأساس قانوني وإفراجه المبكر بمخول وسبب موثق + بيانات رحلات بالحد الأدنى وتنسيق موثق مع جهات المنفذ",
        subtitleEn: "Traveler health reports with risk assessment, screening, isolation and referral at ports of entry + a high-risk symptomatic traveler is isolated or referred, never cleared + quarantine on a legal basis with early release only by an authorized releaser for a documented reason + minimal travel data and recorded coordination with port authorities",
        endpoint: "/api/port-health/reports",
        envelopeKey: "reports",
        columns: [
          { key: "travelerCode", ar: "كود المسافر", en: "Traveler Code" },
          { key: "travelRef", ar: "الرحلة", en: "Travel Ref" },
          { key: "riskLevel", ar: "الخطورة", en: "Risk" },
        ],
        demoRows: [{ "id": "ph1", "travelerCode": "TRV-88", "travelRef": "MS-986", "riskLevel": "high" }],
      }}
    />
  );
}
