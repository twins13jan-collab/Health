import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "quality",
        subtitleAr: "بلاغات سلامة لا تُغلق قبل سبب جذري وإجراء تصحيحي + شكاوى بآجال SLA",
        subtitleEn: "Safety incidents closed only after root cause and corrective action + SLA-bound complaints",
        endpoint: "/api/incidents",
        envelopeKey: "incidents",
        columns: [
          { key: "category", ar: "الفئة", en: "Category" },
          { key: "severity", ar: "الخطورة", en: "Severity" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
        demoRows: [{"id": "d1", "category": "fall", "severity": "moderate", "status": "under_review"}, {"id": "d2", "category": "medication", "severity": "near_miss", "status": "action_assigned"}],
      }}
    />
  );
}
