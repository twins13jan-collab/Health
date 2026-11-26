import ModuleScreen from "@/components/ModuleScreen";

export default function Page() {
  return (
    <ModuleScreen
      config={{
        titleKey: "radiology",
        subtitleAr: "طلبات تصوير بسؤال سريري مبرر للإشعاع ومسار حتى التقرير",
        subtitleEn: "Imaging orders with a justified clinical question, tracked to report",
        endpoint: "/api/radiology/orders",
        envelopeKey: "orders",
        columns: [
          { key: "modality", ar: "الوسيلة", en: "Modality" },
          { key: "bodyPart", ar: "الموضع", en: "Body part" },
          { key: "priority", ar: "الأولوية", en: "Priority" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
