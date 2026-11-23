import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "cssd",
        subtitleAr: "سلسلة إعادة معالجة موثقة لكل طقم ولا إفراج قبل اكتمالها ونجاح المؤشر الكيميائي + أحمال الغرسات تنتظر نجاح المؤشر البيولوجي + فشل بعد الإفراج يستدعي الطقم ولا يعيده للرف + طقم منتهي الصلاحية لا يصرف أبدًا + كل استخدام مرتبط بإجرائه ومريضه",
        subtitleEn: "A documented reprocessing chain per set and no release before it completes with a passed chemical indicator + implant loads wait for a passed biological indicator + a failure after release recalls the set, never re-shelves it + expired sets are never issued + every use is linked to its procedure and patient",
        endpoint: "/api/cssd/sets",
        envelopeKey: "sets",
        columns: [
          { key: "setCode", ar: "الكود", en: "Code" },
          { key: "name", ar: "الطقم", en: "Set" },
          { key: "expiryDate", ar: "الصلاحية", en: "Expiry" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
        demoRows: [{ "id": "cs1", "setCode": "SET-ORTHO-01", "name": "طقم عظام أساسي", "expiryDate": "2027-05-01", "status": "sterile" }],
      }}
    />
  );
}
