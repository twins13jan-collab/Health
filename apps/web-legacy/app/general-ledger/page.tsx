import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "ledger",
        subtitleAr: "قيد غير متوازن يرفض + قيد مرحل لا يعدل أبدًا وتصحيحه بقيد عكسي فقط + ميزان مراجعة يعاد إنتاجه من القيود المرحلة + فترة مغلقة لا تفتح إلا بموافقة وسبب وسجل أثر + حركة بنكية بلا مرجع تبقى غير مطابقة",
        subtitleEn: "An unbalanced journal is refused + a posted journal is never edited and is corrected only by reversal + the trial balance is reproduced from posted journals + a closed period reopens only with approval, a reason and an audit trail + a bank movement without a reference stays unmatched",
        endpoint: "/api/ledger/trial-balance?periodKey=2026-11",
        envelopeKey: "rows",
        columns: [
          { key: "account", ar: "الحساب", en: "Account" },
          { key: "debit", ar: "مدين", en: "Debit" },
          { key: "credit", ar: "دائن", en: "Credit" },
          { key: "balance", ar: "الرصيد", en: "Balance" },
        ],
      }}
    />
  );
}
