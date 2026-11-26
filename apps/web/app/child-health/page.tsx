import ModuleScreen from "@/components/ModuleScreen";

export default function Page() {
  return (
    <ModuleScreen
      config={{
        titleKey: "childHealth",
        subtitleAr: "سجل طفل ونمو وتغذية وتطور + جداول تطعيم وجرعات متأخرة واستدراك + سلسلة تبريد وتشغيلات: جرعة من سلسلة منكسرة ليست جرعة وتُعاد + أحداث تالية للتطعيم تُحقق قبل جرعة اللقاح نفسه التالية + كشف مبكر عن إعاقات واضطرابات نمائية وبرامج تغذية ومكافحة سوء تغذية ومتابعة مزمنين واحتياجات معقدة",
        subtitleEn: "A child record with growth, nutrition and development + immunization schedules, late doses and catch-up + cold chain and batches: a dose from a broken cold chain is not a dose and is repeated + adverse events following immunization investigated before the next dose of the same vaccine + early detection of disabilities and developmental disorders, nutrition programmes, malnutrition control, and follow-up of chronic and complex-needs children",
        endpoint: "/api/child-health/children",
        envelopeKey: "children",
        columns: [
          { key: "childRef", ar: "الطفل", en: "Child" },
          { key: "birthDate", ar: "تاريخ الميلاد", en: "Birth date" },
        ],
      }}
    />
  );
}
