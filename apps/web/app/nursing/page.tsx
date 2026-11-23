import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "nursing",
        subtitleAr: "تقييم تمريضي لمخاطر السقوط وقرح الضغط والألم والتغذية والحركة وخطط رعاية فردية بمواعيد إعادة تقييم + إسناد المهام حسب الترخيص والكفاءة وعبء العمل (الدم بنطاقه، وسقف 12 مهمة مفتوحة) + مهام متأخرة تتصاعد ولا تختفي + تسليم ورديات منظم بالحالات الحرجة والمهام المفتوحة",
        subtitleEn: "Nursing assessment of fall, pressure-ulcer, pain, nutrition and mobility risks with individual care plans and reassessment dates + tasks assigned by licence, competence and workload (blood within its scope, a 12-open-task cap) + overdue tasks escalate instead of vanishing + structured shift handover around critical cases and open tasks",
        endpoint: "/api/nursing/assessments",
        envelopeKey: "assessments",
        columns: [
          { key: "patientRef", ar: "المريض", en: "Patient" },
          { key: "fallRisk", ar: "خطر السقوط", en: "Fall Risk" },
          { key: "painScore", ar: "الألم", en: "Pain" },
        ],
        demoRows: [{ "id": "n1", "patientRef": "P-800", "fallRisk": "high", "painScore": 6 }],
      }}
    />
  );
}
