import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "clinicalTrials",
        subtitleAr: "سجل تجارب ومراكز وباحثين وبروتوكولات + لا توظيف قبل موافقة الأخلاقيات والتنظيم وإعلان تضارب المصالح + المشارك بكود فقط وفصل بيانات البحث عن العلاج + أهلية وموافقة موثقة قبل أي زيارة أو عينة + الانسحاب فوري + الحدث الضار الخطير غير المبلَّغ للأخلاقيات خرق ظاهر",
        subtitleEn: "Registry of trials, centres, investigators and protocols + no recruitment before ethics and regulatory approval with conflicts declared + participants by code only, research data separated from care + eligibility and documented consent before any visit or sample + immediate withdrawal + an unreported serious adverse event is a visible breach",
        endpoint: "/api/clinical-trials/",
        envelopeKey: "trials",
        columns: [
          { key: "title", ar: "التجربة", en: "Trial" },
          { key: "phase", ar: "المرحلة", en: "Phase" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
        demoRows: [{ "id": "ct1", "title": "دراسة دواء ضغط جديد", "phase": "III", "status": "recruiting" }],
      }}
    />
  );
}
