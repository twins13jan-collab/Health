import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "medicalAssets",
        subtitleAr: "كل أصل بمعرف ومالك وموقع ودورة حياة كاملة + جهاز خارج المعايرة يوسم غير صالح عند التكامل + مبلغ العطل لا يعيد الجهاز للخدمة أبدًا والإفراج للهندسة الطبية والجودة + عطل مؤثر على مريض يفتح بلاغ سلامة",
        subtitleEn: "Every asset has an identity, owner, location and a full lifecycle + an out-of-calibration device is flagged unfit at integration + the fault reporter never returns a device to service and release belongs to biomedical engineering and quality + a patient-affecting fault opens a safety case",
        endpoint: "/api/medical-assets",
        envelopeKey: "assets",
        columns: [
          { key: "assetCode", ar: "الكود", en: "Code" },
          { key: "type", ar: "النوع", en: "Type" },
          { key: "location", ar: "الموقع", en: "Location" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
        demoRows: [{ "id": "ma1", "assetCode": "VENT-014", "type": "جهاز تنفس صناعي", "location": "العناية المركزة", "status": "in_service" }],
      }}
    />
  );
}
