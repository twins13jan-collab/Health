import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "palliative",
        subtitleAr: "رغبة المريض لا تستبدلها خوارزمية: التمثيل القانوني يتحقق ولا يُفترض + وثيقة غير موثقة لا تلغي موثقة + استجابة الألم تُقاس قبل وبعد + ألم شديد يستدعي استجابة عاجلة",
        subtitleEn: "The patient's wish is never replaced by an algorithm: legal representation is verified, never assumed + an unverified document cannot supersede a verified one + pain response is measured before and after + severe pain triggers an urgent response",
        endpoint: "/api/palliative/assessments?patientId=00000000-0000-0000-0000-000000000000",
        envelopeKey: "assessments",
        columns: [
          { key: "symptoms", ar: "الأعراض", en: "Symptoms" },
          { key: "painScore", ar: "شدة الألم", en: "Pain score" },
          { key: "functionalStatus", ar: "الحالة الوظيفية", en: "Functional status" },
          { key: "assessedAt", ar: "التاريخ", en: "Date" },
        ],
      }}
    />
  );
}
