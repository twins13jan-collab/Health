import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "pathology",
        subtitleAr: "الهوية لا تُخمن أبدًا: عينة غير موثقة تُحجر + أولي ليس نهائيًا والنهائي موقع + تقرير معتمد لا يُحرر بل يُضاف إليه بإصدار يخطر المستلمين السابقين + رأيان محفوظان معًا",
        subtitleEn: "Identity is never guessed: an unverified specimen is quarantined + preliminary is not final and finals are signed + an approved report is never edited, only amended by a version that notifies previous recipients + both opinions are preserved",
        endpoint: "/api/pathology/orders?patientId=00000000-0000-0000-0000-000000000000",
        envelopeKey: "orders",
        columns: [
          { key: "specimenType", ar: "نوع العينة", en: "Specimen type" },
          { key: "site", ar: "الموقع", en: "Site" },
          { key: "urgency", ar: "الاستعجال", en: "Urgency", status: true },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
        demoRows: [{ "id": "pa1", "specimenType": "خزعة قولون", "site": "القولون الصاعد", "urgency": "urgent", "status": "reported" }],
      }}
    />
  );
}
