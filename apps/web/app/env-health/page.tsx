import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "envHealth",
        subtitleAr: "بيانات طقس وحرارة وجودة هواء ومياه وتلوث من مصادر موثوقة فقط — تنبيه رسمي لا يخرج من حساس رديء + تحليل مخاطر حرارة وسيول وعواصف وتلوث وخطط استجابة للسكان المتأثرين + سلامة مياه شرب وصرف ونفايات: نتيجة غير آمنة لا تُغلق قبل إجراء حماية فعلي",
        subtitleEn: "Weather, heat, air-quality, water and pollution data from reliable sources only — no official alert from a rumour-grade sensor + risk analysis for heat, floods, storms and pollution with response plans for affected populations + drinking-water, sewage and waste safety: an unsafe result never closes before a real protective action",
        endpoint: "/api/env-health/alerts",
        envelopeKey: "alerts",
        columns: [
          { key: "hazardType", ar: "الخطر", en: "Hazard" },
          { key: "level", ar: "المستوى", en: "Level" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
        demoRows: [{ "id": "eh1", "hazardType": "heatwave", "level": "warning", "status": "open" }],
      }}
    />
  );
}
