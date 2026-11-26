import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "specialPopulations",
        subtitleAr: "خدمات بمدارس وجامعات ومراكز شباب ومنشآت رياضية + رعاية بأماكن الاحتجاز بضمانات قانونية وحقوق مرضى مؤكدة قبل إغلاق أي سجل + لاجئون ونازحون وفئات متنقلة بلا عوائق غير مبررة — حجب رعاية بسبب وثائق أو رسوم يُعلَّم تلقائيًا لمراجعة إنكار + نقاط خدمة بمنافذ ومطارات وموانئ وتغطية فعاليات وتجمعات",
        subtitleEn: "Services in schools, universities, youth centres and sports facilities + care in detention under confirmed legal safeguards and patient rights before any record closes + refugees, displaced and mobile people without unjustified barriers — care withheld over documents or fees is auto-flagged for denial review + service points at ports and airports and cover for events and mass gatherings",
        endpoint: "/api/special-populations/points",
        envelopeKey: "points",
        columns: [
          { key: "name", ar: "النقطة", en: "Point" },
          { key: "siteType", ar: "النوع", en: "Type" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
