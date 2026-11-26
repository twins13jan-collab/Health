import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "criticalCare",
        subtitleAr: "معايير قبول ونقل وخروج من العناية + تقييم متعدد التخصصات ودعم أعضاء + أجهزة وأدوية عالية الخطورة + نقل مريض حرج بفريق وتجهيزات مناسبة وتسليم حالة ومراجعة استقرار — والنقل يقف على أربعة شروط معًا: معايير مستوفاة واستقرار مراجع وفريق مجهز وتسليم موثق؛ سرير شاغر حقيقة لوجستية لا قرار سريري",
        subtitleEn: "Intensive-care admission, transfer and discharge criteria + multidisciplinary assessment and organ support + high-risk devices and medications + critical-patient transfer with a fitting team and equipment, case handover and stability review — a transfer stands on all four together: criteria met, stability reviewed, team equipped and handover documented; a free bed is a logistics fact, not a clinical decision",
        endpoint: "/api/critical-care/transfers",
        envelopeKey: "transfers",
        columns: [
          { key: "patientRef", ar: "المريض", en: "Patient" },
          { key: "toLevel", ar: "إلى مستوى", en: "To level" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
