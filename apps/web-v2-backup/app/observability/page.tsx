import ModuleScreen from "@/components/ModuleScreen";

export default function Page() {
  return (
    <ModuleScreen
      config={{
        titleKey: "observability",
        subtitleAr: "مراقبة صحة الخدمات والواجهات وقواعد البيانات والرسائل والتكاملات والأجهزة الطرفية + زمن استجابة ومعدلات خطأ وتأخير وتراكم طوابير واستهلاك موارد — والاختراق يظهر لا يُذاب في متوسط، وحادث تشغيلي لا يغلق بلا سجل معالجة، والحرج يمر بالتصعيد أولًا، ولوحات تقنية منفصلة عن لوحات إدارية",
        subtitleEn: "The health of services, APIs, databases, messages, integrations and edge devices monitored + response times, error rates, delays, queue backlogs and resource consumption — a breach is surfaced, never averaged away, an operational incident closes only with a resolution record, a critical one passes through escalation first, and technical dashboards stay separate from management dashboards",
        endpoint: "/api/observability/components",
        envelopeKey: "components",
        columns: [
          { key: "componentRef", ar: "المكون", en: "Component" },
          { key: "componentType", ar: "النوع", en: "Type" },
        ],
      }}
    />
  );
}
