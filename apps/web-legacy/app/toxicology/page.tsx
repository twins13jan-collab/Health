import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "toxicology",
        subtitleAr: "عدم اليقين يظهر ولا يختفي: مادة مجهولة توصف عبوتها دون افتراض + كل توصية بمستشار ومرجع محدث ووقت + موظف البلاغ يسجل ولا ينصح + أنماط التعرض مجمعة بلا هوية",
        subtitleEn: "Uncertainty is shown, never hidden: an unknown substance is described by its container, never assumed + every recommendation binds a consultant, a current reference and a time + intake staff record but never advise + exposure patterns are de-identified aggregates",
        endpoint: "/api/toxicology/cases",
        envelopeKey: "cases",
        columns: [
          { key: "substanceName", ar: "المادة", en: "Substance" },
          { key: "route", ar: "طريق التعرض", en: "Route" },
          { key: "severity", ar: "الشدة", en: "Severity", status: true },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
