import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "anomalyRegistry",
        subtitleAr: "سجل قومي بتعريفات حالات ومصادر إبلاغ موحدة وربط حمل وولادة ومولود وفق الصلاحيات + المؤكد والمشتبه لا يُدمجان في الأرقام المنشورة أبدًا + تحليل اتجاهات يصف الأنماط دون استنتاج سببي غير مدعوم + إحالة الأسر للوراثة والتأهيل",
        subtitleEn: "A national registry with standardized case definitions and reporting sources, pregnancy/birth/newborn linkage under permission + confirmed and suspected cases are never merged in published figures + trend analysis describes patterns without unsupported causal claims + families referred to genetics and rehabilitation",
        endpoint: "/api/anomaly-registry/cases",
        envelopeKey: "cases",
        columns: [
          { key: "caseCode", ar: "كود الحالة", en: "Case Code" },
          { key: "anomalyCode", ar: "التشوه", en: "Anomaly" },
          { key: "classification", ar: "التصنيف", en: "Class" },
        ],
      }}
    />
  );
}
