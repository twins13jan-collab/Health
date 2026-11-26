import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "hta",
        subtitleAr: "تقييم أدوية وأجهزة وإجراءات وتقنيات عبر الفاعلية والسلامة والتكلفة والجدوى والأثر الأخلاقي + اللجنة لا تقيّم قبل إعلان تضارب المصالح كاملًا ومشاركة أصحاب المصلحة + قوة التوصية محدودة بجودة الأدلة: الضعيفة نتيجتها بحث فقط أو رفض مهما حماس الطالب",
        subtitleEn: "Drugs, devices, procedures and technologies assessed across effectiveness, safety, cost, budget and ethical impact + no appraisal before complete conflict-of-interest declarations and stakeholder participation + recommendation strength is capped by evidence quality: weak evidence yields research-only or rejection however enthusiastic the requester",
        endpoint: "/api/hta/assessments",
        envelopeKey: "assessments",
        columns: [
          { key: "technologyName", ar: "التقنية", en: "Technology" },
          { key: "techType", ar: "النوع", en: "Type" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
