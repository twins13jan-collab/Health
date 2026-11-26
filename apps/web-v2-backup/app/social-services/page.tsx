import ModuleScreen from "@/components/ModuleScreen";

export default function Page() {
  return (
    <ModuleScreen
      config={{
        titleKey: "socialServices",
        subtitleAr: "تقييم اجتماعي واحتياجات دعم وإحالات نفسية واجتماعية + خطر أزمة يفرض خطة سلامة قبل أي شيء + قلق حماية على طفل أو بالغ معرض للخطر يفرض إحالة لجهة الحماية ولا تُغلق خطة الدعم قبلها + سجل اجتماعي خلف جدران أدوار — ليس من مادة الاستقبال",
        subtitleEn: "Social assessment and support needs with psychological and social referrals + crisis risk demands a safety plan before anything else + a safeguarding concern for a child or vulnerable adult mandates referral to the protection authority and the support plan cannot close before it + the social record stays behind role walls — not front-desk material",
        endpoint: "/api/social-services/assessments",
        envelopeKey: "assessments",
        columns: [
          { key: "patientRef", ar: "المريض", en: "Patient" },
          { key: "riskLevel", ar: "مستوى الخطر", en: "Risk level" },
          { key: "safeguardingConcern", ar: "قلق الحماية", en: "Safeguarding" },
        ],
      }}
    />
  );
}
