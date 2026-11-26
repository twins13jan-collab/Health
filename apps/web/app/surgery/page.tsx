import ModuleScreen from "@/components/ModuleScreen";

export default function Page() {
  return (
    <ModuleScreen
      config={{
        titleKey: "surgery",
        subtitleAr: "حالات جراحية مقيدة بقائمة WHO — لا تقدم قبل اكتمال المرحلة (دورة 220)",
        subtitleEn: "Surgical cases gated by the WHO checklist — no advance before stage completion (cycle 220)",
        endpoint: "/api/surgery/cases",
        envelopeKey: "cases",
        columns: [
          { key: "procedureName", ar: "الإجراء", en: "Procedure" },
          { key: "operatingRoom", ar: "الغرفة", en: "OR" },
          { key: "checklistStage", ar: "مرحلة القائمة", en: "Checklist stage" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
