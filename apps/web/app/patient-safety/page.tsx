import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "patientSafety",
        subtitleAr: "تصنيف حوادث ووقائع وشيكة + إبلاغ سري وحماية مبلغين — هوية مكشوفة صمت مشترى + إخطار مريض متضرر وفق السياسة والقانون + تحليل أسباب نظامية وبشرية + خطط منع تكرار وقياس أثر: الحادث لا يغلق قبل الإخطار والتحليل واكتمال كل إجراءات المنع",
        subtitleEn: "Incidents and near misses classified + confidential reporting and protected reporters — an identity exposed is a silence purchased + the harmed patient notified per policy and law + systemic and human causes analyzed + recurrence-prevention plans and measured impact: an incident closes only after notification, analysis and every prevention action completed",
        endpoint: "/api/patient-safety/incidents",
        envelopeKey: "incidents",
        columns: [
          { key: "incidentType", ar: "النوع", en: "Type" },
          { key: "severity", ar: "الخطورة", en: "Severity" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
        demoRows: [{ "id": "ps1", "incidentType": "near_miss", "severity": "moderate", "status": "reported" }],
      }}
    />
  );
}
