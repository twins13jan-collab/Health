import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "medicalTourism",
        subtitleAr: "طلبات مرضى دوليين وملفات وترجمة وموافقات + تنسيق مواعيد وتقديرات مالية وإقامة ونقل ولوجستيات + إحالات دولية وتقارير وتواصل مع جهات مرسلة — والجدولة بعد ملف مترجم وموافقة وتقدير معتمد، والبيانات لا تعبر الحدود إلا بأساس قانوني وموافقة معًا، ومتابعة ما بعد العودة بإذن المريض فقط",
        subtitleEn: "International patient requests, files, translation and consents + appointments, financial estimates, accommodation, transport and logistics coordinated + international referrals, reports and referring-body communication — scheduling after a translated file, consent and an approved estimate, data crossing borders only on a legal basis AND consent together, and post-return follow-up only as the patient permits",
        endpoint: "/api/medical-tourism/requests",
        envelopeKey: "requests",
        columns: [
          { key: "patientRef", ar: "المريض", en: "Patient" },
          { key: "country", ar: "الدولة", en: "Country" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
