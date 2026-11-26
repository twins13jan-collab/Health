import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "recalls",
        subtitleAr: "دفتر حركات لا يُحرر أبدًا والرصيد يُبنى منه + صرف بلا منتهٍ أو محجور وبانضباط FEFO موثق الاستثناء + استدعاء يحظر الصرف في كل المواقع ولا يغلق قبل تصرف كامل باعتماد الجودة",
        subtitleEn: "An append-only movement ledger that balances are rebuilt from + no dispensing of expired or quarantined stock under documented FEFO + a recall blocks dispensing everywhere and closes only on full quality-approved disposition",
        endpoint: "/api/recalls/batches?itemId=00000000-0000-0000-0000-000000000000",
        envelopeKey: "batches",
        columns: [
          { key: "batchNumber", ar: "التشغيلة", en: "Batch" },
          { key: "expiryDate", ar: "الانتهاء", en: "Expiry" },
          { key: "manufacturer", ar: "الشركة", en: "Manufacturer" },
          { key: "releaseStatus", ar: "حالة الإفراج", en: "Release status", status: true },
        ],
      }}
    />
  );
}
