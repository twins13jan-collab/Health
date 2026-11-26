import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "chronicDeep",
        subtitleAr: "قوائم مرضى مستهدفين بمعايير معلنة + خطط متابعة سكري وضغط وقلب وربو وكلى + قياسات منزلية بتحقق من المصدر: القراءة غير الموثقة تُسجل وتُوسم ولا توجه رعاية أبدًا + تذكير بمواعيد وفحوص وأدوية + تقييم مخاطر مضاعفات وإحالة مبكرة وقياس سيطرة وانقطاع ومتابعة وبرامج كشف مبكر بمعايير أهلية",
        subtitleEn: "Target patient lists under declared criteria + follow-up plans for diabetes, hypertension, cardiac, asthma and renal disease + home measurements with source verification: an unverified reading is recorded and flagged, and never steers care + appointment, test and medication reminders + complication-risk assessment with early referral, control, dropout and follow-up measurement, and early-detection programmes with eligibility criteria",
        endpoint: "/api/chronic-deep/enrollments",
        envelopeKey: "enrollments",
        columns: [
          { key: "patientRef", ar: "المريض", en: "Patient" },
          { key: "conditionName", ar: "الحالة", en: "Condition" },
          { key: "criteriaRef", ar: "المعايير", en: "Criteria" },
        ],
      }}
    />
  );
}
