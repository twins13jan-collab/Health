import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "diseaseRegistries",
        subtitleAr: "سجلات أمراض بتعريفات حالة موحدة — من لا يستوفي التعريف لا يدخل + ربط بالنتائج والمتابعة وبروتوكولات بحث وموافقاتها + جودة بيانات وفقد متابعة وتحيز معلنة: نشر مجمع لمجموعة ≥30 فقط، وفوق 30% فقدًا مذكرة تحيز إلزامية فالنشر الصامت للباقين كذب إحصائي",
        subtitleEn: "Disease registries on unified case definitions — whoever does not meet the definition does not enter + linkage to outcomes, follow-up, research protocols and approvals + data quality, loss to follow-up and bias declared openly: aggregate publication only for cohorts of 30+, and above 30% loss a bias note is mandatory, because silently publishing the survivors is a statistical lie",
        endpoint: "/api/disease-registries/registries",
        envelopeKey: "registries",
        columns: [
          { key: "name", ar: "السجل", en: "Registry" },
          { key: "caseDefinitionRef", ar: "تعريف الحالة", en: "Case definition" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
