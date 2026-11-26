import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "hrDeep",
        subtitleAr: "ملف وظيفي وتعيين وعقود ونقل + حضور وإجازات وورديات وعمل إضافي + رواتب وبدلات واستقطاعات + أداء وكفاءات وتطوير + تدريب وتعليم مستمر — والدور السريري بترخيص منتهٍ غير قابل للإسناد، والتدريب الإلزامي المتأخر عدم امتثال موسوم لا امتثال صامت، والتظلم لا يغلق قبل مراجعة ونتيجة مسجلة",
        subtitleEn: "The employment file, appointment, contracts and transfers + attendance, leave, shifts and overtime + payroll, allowances and deductions + performance, competencies and development + training and continuing education — a clinical role with a lapsed licence is not assignable, overdue mandatory training is flagged non-compliance rather than silent compliance, and a grievance closes only after review with a recorded outcome",
        endpoint: "/api/hr-deep/employees",
        envelopeKey: "employees",
        columns: [
          { key: "staffRef", ar: "الموظف", en: "Staff" },
          { key: "role", ar: "الدور", en: "Role" },
          { key: "licenceValid", ar: "الترخيص سارٍ", en: "Licence valid" },
        ],
      }}
    />
  );
}
