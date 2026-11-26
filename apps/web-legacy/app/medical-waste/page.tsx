import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "medicalWaste",
        subtitleAr: "خطط نظافة حسب خطورة المنطقة وتدقيقها + النفايات مفروزة وموزونة وبسلسلة تسليم كاملة ومتعهد مرخص سارٍ يوم الجمع + تخزين النفايات المعدية فوق 48 ساعة إخلال موثق + الانسكاب الخطر يغلق بعد الاحتواء والتطهير معًا",
        subtitleEn: "Risk-based cleaning plans, audited + waste segregated, weighed and manifested with a complete chain of custody and a contractor licensed on collection day + infectious waste stored over 48 hours is a documented breach + a hazardous spill closes only when contained AND decontaminated",
        endpoint: "/api/medical-waste/manifests",
        envelopeKey: "manifests",
        columns: [
          { key: "wasteType", ar: "النوع", en: "Type" },
          { key: "sourceUnit", ar: "المصدر", en: "Source" },
          { key: "weightKg", ar: "الوزن كجم", en: "Weight kg" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
