import ModuleScreen from "@/components/ModuleScreen";

export default function Page() {
  return (
    <ModuleScreen
      config={{
        titleKey: "softwareLifecycle",
        subtitleAr: "سجل تطبيقات وخدمات ومكونات وإصدارات ببيئات تطوير واختبار وتجهيز وإنتاج منفصلة وإعدادات بلا نسخ متباينة + إنتاج لا يستقبل إلا مراجعًا وموقعًا وبلا انجراف إعدادات، ومكون طرف ثالث بلا ترخيص موثق لا يشحن + تراجع رسمي بسبب موثق",
        subtitleEn: "A register of applications, services, components and versions with development, test, staging and production kept separate and configuration without divergent copies + production accepts only what is reviewed, signed and drift-free, and a third-party component without a licence on file does not ship + rollback as a formal, reasoned act",
        endpoint: "/api/software-lifecycle/components",
        envelopeKey: "components",
        columns: [
          { key: "name", ar: "المكون", en: "Component" },
          { key: "kind", ar: "النوع", en: "Kind" },
          { key: "thirdParty", ar: "طرف ثالث", en: "Third-party" },
        ],
      }}
    />
  );
}
