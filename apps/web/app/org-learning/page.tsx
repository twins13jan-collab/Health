import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "orgLearning",
        subtitleAr: "مكتبة موحدة لسياسات وإجراءات وأدلة تشغيل وتعليمات سريرية وإدارية، كل وثيقة بمالك وإصدار وسريان ومراجعة ومستفيدين — والوثيقة منتهية المراجعة خطر بزي رسمي، والقراءة ليست فهمًا: الإقرار لا يحسب إلا بتقييم استيعاب، والدرس المستفاد لا يغلق إلا مطبقًا في وثيقة حية",
        subtitleEn: "A unified library of policies, procedures, operating guides and clinical and administrative instructions, every document with an owner, version, effective date, review date and beneficiaries — an expired-review document is a hazard in uniform, reading is not understanding: acknowledgment counts only with a comprehension assessment, and a lesson learned closes only when applied into a living document",
        endpoint: "/api/org-learning/documents",
        envelopeKey: "documents",
        columns: [
          { key: "title", ar: "الوثيقة", en: "Document" },
          { key: "ownerRef", ar: "المالك", en: "Owner" },
          { key: "version", ar: "الإصدار", en: "Version" },
        ],
        demoRows: [{ "id": "ol1", "title": "دليل الإخلاء عند الحريق", "ownerRef": "إدارة السلامة", "version": "v3" }],
      }}
    />
  );
}
