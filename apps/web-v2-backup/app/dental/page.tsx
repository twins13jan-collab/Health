import ModuleScreen from "@/components/ModuleScreen";

export default function Page() {
  return (
    <ModuleScreen
      config={{
        titleKey: "dental",
        subtitleAr: "لا إجراء على موقع غير موثق ولا بلا موافقة ولا بأدوات تعقيم فاشلة + كل إجراء متتبع إلى السن والطبيب والمادة والتشغيلة + مخطط أسنان بإصدارات وزرعات قابلة للاستدعاء بالتشغيلة",
        subtitleEn: "No procedure on an unverified site, without consent, or with failed-sterilization instruments + every procedure traces to tooth, dentist, material and lot + versioned dental charts and lot-recallable implants",
        endpoint: "/api/dental/procedures?patientId=00000000-0000-0000-0000-000000000000",
        envelopeKey: "procedures",
        columns: [
          { key: "procedureCode", ar: "الإجراء", en: "Procedure" },
          { key: "toothNumber", ar: "السن (FDI)", en: "Tooth (FDI)" },
          { key: "dentist", ar: "الطبيب", en: "Dentist" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
