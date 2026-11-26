import ModuleScreen from "@/components/ModuleScreen";

export default function Page() {
  return (
    <ModuleScreen
      config={{
        titleKey: "researchGovernance",
        subtitleAr: "طلبات استخدام بيانات لبحث وتخطيط وتقييم وسياسات عامة بمراجعة غرض وضرورة وحد أدنى من البيانات وأساس قانوني وموافقات ولجان أخلاقيات وتصاريح وصول وباحثين ومخرجات دراسات — والبيانات المجمعة أو منزوعة الهوية متى أمكن، والمعرّفة استثناء مبرر فقط، ومنع إعادة التعرف ومراقبة تصدير واستخدام لاحق واتفاقيات مشاركة واضحة مع جامعات وجهات بحثية",
        subtitleEn: "Data-use requests for research, planning, evaluation and public policy, reviewed for purpose, necessity, the minimum dataset, legal basis, approvals, ethics committees, access permits, researchers and study outputs — aggregate or de-identified data whenever appropriate, identified data only as a justified exception, re-identification prevented, export and later use monitored, and sharing with universities and research bodies under clear agreements",
        endpoint: "/api/research-governance/requests",
        envelopeKey: "requests",
        columns: [
          { key: "purpose", ar: "الغرض", en: "Purpose" },
          { key: "dataForm", ar: "شكل البيانات", en: "Data form" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
