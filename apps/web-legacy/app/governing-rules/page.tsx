import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "governingRules",
        subtitleAr: "دستور المنظومة مطبقًا على نفسه: عزلة بلا استثناء، وكل سجل حساس بمصدر ومالك وتدقيق، وكل تكامل بهوية موثقة وإيقاف ومراجعة، وكل قاعدة سريرية بإصدار ومصدر واعتماد وسريان، وكل ذكاء استشاري والقرار للمختص، وكل مشاركة بيانات بغرض وأساس وحد أدنى — والامتثال لا يتحقق إلا بدليل وفحص مستقل: لا اكتمال يُعلن بلا أدلة",
        subtitleEn: "The system's constitution enforced on itself: isolation without exception, every sensitive record with source, owner and audit, every integration with an authenticated identity, a kill mechanism and review, every clinical rule versioned, sourced, accredited and effective-dated, every AI output advisory with the decision kept by the specialist, every data sharing bound by purpose, basis and the minimum necessary — and compliance is verified only with evidence and independent checking: no completion declared without proof",
        endpoint: "/api/governing-rules/rules",
        envelopeKey: "rules",
        columns: [
          { key: "ruleCode", ar: "الرمز", en: "Code" },
          { key: "title", ar: "القاعدة", en: "Rule" },
        ],
      }}
    />
  );
}
