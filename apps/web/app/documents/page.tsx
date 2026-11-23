import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "documents",
        subtitleAr: "كل وثيقة بمالك وتصنيف وإصدار وبصمة ومصدر + توقيع بشهادة غير صالحة لا يعد اعتمادًا + تعديل بعد التوقيع ينشئ إصدارًا جديدًا ولا يرث التوقيع + سجل محمي بحفظ قانوني أو التزام احتفاظ لا يحذف أبدًا + كل عرض وطباعة وتنزيل ومشاركة مسجل بغرضه",
        subtitleEn: "Every document has an owner, classification, version, fingerprint and source + a signature with an invalid certificate is not an approval + editing after signing mints a new version that does not inherit the signature + a legally held or retention-bound record is never deleted + every view, print, download and share is logged with its purpose",
        endpoint: "/api/records-documents",
        envelopeKey: "documents",
        columns: [
          { key: "docType", ar: "النوع", en: "Type" },
          { key: "title", ar: "العنوان", en: "Title" },
          { key: "currentVersion", ar: "الإصدار", en: "Version" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
        demoRows: [{ "id": "dc1", "docType": "تقرير طبي", "title": "تقرير خروج", "currentVersion": 2, "status": "approved" }],
      }}
    />
  );
}
