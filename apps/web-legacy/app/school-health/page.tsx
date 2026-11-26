import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "schoolHealth",
        subtitleAr: "فحوص وتطعيمات وتثقيف وإحالات بموافقة ولي الأمر — مسجلة للفحص ومتحقق منها مجددًا لحظة التطعيم + أمراض مزمنة وحساسية وطوارئ لكل طالب + تقارير مجمعة فقط وبحد أدنى عشرة طلاب + دعم الصحة النفسية سري لأدوار الإرشاد والإدارة",
        subtitleEn: "Screenings, vaccinations, education and referrals on guardian consent — recorded for screening and verified again at the moment of vaccination + chronic conditions, allergies and emergencies per student + aggregate-only reports with a ten-student minimum + mental-health support confidential to counsellor and administration roles",
        endpoint: "/api/school-health/students",
        envelopeKey: "students",
        columns: [
          { key: "studentCode", ar: "كود الطالب", en: "Student Code" },
          { key: "institutionRef", ar: "المؤسسة", en: "Institution" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
