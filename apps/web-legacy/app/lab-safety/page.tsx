import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "labSafety",
        subtitleAr: "تقييم مخاطر قبل إدخال أي اختبار وتحقق من الدقة والحساسية والتداخلات ومقارنة نتائج بين الأجهزة ضمن ±10% + دفعات كواشف بصلاحية — المنتهية خطر لا كاشف + عطل أي جهاز يوقف خدمته حتى يُقيَّم أثره على النتائج السابقة",
        subtitleEn: "Risk assessment before any test is introduced, with accuracy, sensitivity and interferences verified and cross-analyzer results compared within ±10% + reagent lots within validity — an expired lot is a hazard, not a reagent + an analyzer failure takes it out of service until the impact on previous results is assessed",
        endpoint: "/api/lab-safety/introductions",
        envelopeKey: "introductions",
        columns: [
          { key: "testName", ar: "الاختبار", en: "Test" },
          { key: "analyzer", ar: "الجهاز", en: "Analyzer" },
          { key: "approved", ar: "معتمد", en: "Approved" },
        ],
      }}
    />
  );
}
