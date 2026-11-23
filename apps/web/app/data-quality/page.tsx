import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "dataQuality",
        subtitleAr: "قواعد اكتمال وصحة واتساق وحداثة وعدم تكرار بمالك بيانات ومصدر حقيقة معلن لكل أصل + قاموس بيانات وتعريفات مؤشرات ببسط ومقام + تتبع مفقود ومتعارض وتصحيحاته بسند مصدر الحقيقة + كل رقم منشور بدرجة ثقته: دون 70 لا يُنشر إلا بتحفظاته مرفقة",
        subtitleEn: "Completeness, validity, consistency, freshness and de-duplication rules with a named data owner and declared source of truth for every asset + a data dictionary and indicator definitions with numerator and denominator + missing and conflicting data tracked through corrections citing the source of truth + every published figure carries its confidence grade: below 70 it publishes only with its caveats attached",
        endpoint: "/api/data-quality/assets",
        envelopeKey: "assets",
        columns: [
          { key: "name", ar: "الأصل", en: "Asset" },
          { key: "ownerRef", ar: "المالك", en: "Owner" },
          { key: "completenessPct", ar: "الاكتمال %", en: "Completeness %" },
        ],
        demoRows: [{ "id": "dq1", "name": "سجل اللقاءات", "ownerRef": "إدارة المعلومات الصحية", "completenessPct": 96 }],
      }}
    />
  );
}
