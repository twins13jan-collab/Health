import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "terminologyDeep",
        subtitleAr: "قاموس أحداث سريرية وإدارية ومالية + ربط مصطلحات محلية بمعايير مناسبة وإصدارات قواميس وسريان + تحقق من عدم فقد المعنى في التحويل ومراجعة مختصين: الربط غير الموثق يحول البيانات بفقد صامت لمعناها يظهر بعد سنوات إحصاءات خاطئة + توثيق مصطلحات غير مدعومة كما هي لا قسرها لأقرب رمز",
        subtitleEn: "A dictionary of clinical, administrative and financial events + local terms mapped to the appropriate standards with dictionary versions and effectivity + meaning preservation in conversion verified and specialist review: an unverified mapping converts data by quietly losing its meaning, surfacing years later as wrong statistics + unsupported terms documented as they are, never force-mapped to the nearest code",
        endpoint: "/api/terminology-deep/mappings",
        envelopeKey: "mappings",
        columns: [
          { key: "localTerm", ar: "المصطلح المحلي", en: "Local term" },
          { key: "standardSystem", ar: "المعيار", en: "Standard" },
          { key: "standardCode", ar: "الرمز", en: "Code" },
        ],
      }}
    />
  );
}
