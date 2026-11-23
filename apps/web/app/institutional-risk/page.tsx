import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "institutionalRisk",
        subtitleAr: "سجل مخاطر سريرية وتشغيلية ومالية وقانونية وتقنية باحتمال وأثر وضوابط ومخاطرة متبقية ظاهرة بجانب الأصلية + مالك خطر مسمى وخطة معالجة لكل خطر + إغلاق فقط بمعالجات منفذة كاملة ومتبقٍّ دون القصوى أو قبول رسمي موثق لها + قرارات لجان متتبعة حتى التنفيذ وقياس الأثر",
        subtitleEn: "A register of clinical, operational, financial, legal and technical risks with likelihood, impact, controls and residual risk shown beside the inherent score + a named owner and treatment plan for every risk + closure only with all treatments done and the residual below extreme, or a formal recorded acceptance of it + committee decisions tracked until implemented and their impact measured",
        endpoint: "/api/institutional-risk/risks",
        envelopeKey: "risks",
        columns: [
          { key: "title", ar: "الخطر", en: "Risk" },
          { key: "category", ar: "الفئة", en: "Category" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
        demoRows: [{ "id": "ir1", "title": "انقطاع الشبكة عن العناية المركزة", "category": "technical", "status": "open" }],
      }}
    />
  );
}
