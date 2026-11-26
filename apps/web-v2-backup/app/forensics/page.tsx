import ModuleScreen from "@/components/ModuleScreen";

export default function Page() {
  return (
    <ModuleScreen
      config={{
        titleKey: "forensics",
        subtitleAr: "طلبات فحص وتقارير طبية قانونية وسلسلة حيازة عينات وأدلة منفصلة عن الملف العلاجي + لا فحص قبل التحقق من اختصاص الجهة القضائية وتفويضها + الأدلة تتحرك بسلسلة حيازة لا تنكسر بطرفين مسميين + التقرير يُسلَّم بتوقيعين مختلفين وللجهة الطالبة فقط",
        subtitleEn: "Examination requests, medico-legal reports and the chain of custody of samples and evidence, held apart from the clinical record + no examination before the judicial authority's jurisdiction and mandate are verified + evidence moves on an unbroken custody chain with both parties named + reports released with two distinct signatures, to the requesting authority only",
        endpoint: "/api/forensics/cases",
        envelopeKey: "cases",
        columns: [
          { key: "caseRef", ar: "القضية", en: "Case" },
          { key: "requestingAuthority", ar: "الجهة الطالبة", en: "Authority" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
