import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "majorIncidents",
        subtitleAr: "خطط استجابة متعددة الجهات بسلاسل قيادة واتصال بديل + قوائم فرق احتياطية ومتطوعين موثقين فقط يُنشرون + تدريبات ومحاكاة بدروس مستفادة — خطة بلا تدريب خلال 12 شهرًا لا تُفعَّل + رسائل توعية عامة من مصادر معتمدة فقط ومكافحة شائعات صحية",
        subtitleEn: "Multi-agency response plans with command chains and alternative communications + reserve and volunteer rosters where only the verified deploy + training and simulation with lessons learned — a plan unexercised for 12 months cannot activate + public awareness messaging from verified sources only, with health-rumour control",
        endpoint: "/api/major-incidents/plans",
        envelopeKey: "plans",
        columns: [
          { key: "name", ar: "الخطة", en: "Plan" },
          { key: "agenciesInvolved", ar: "الجهات", en: "Agencies" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
        demoRows: [{ "id": "mi1", "name": "خطة انهيار مبنى سكني", "agenciesInvolved": "صحة + دفاع مدني + إسعاف", "status": "ready" }],
      }}
    />
  );
}
