import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "offlineSync",
        subtitleAr: "عمل محلي داخل حزمة محددة النطاق والمدة على جهاز موثوق مشفر + مزامنة لا تكرر المعاملة أبدًا عند الإعادة + بيانات مرجعية قديمة توقف العمليات الحساسة + تعارض سريري أو مالي لا يحل آليًا أبدًا",
        subtitleEn: "Local work inside a bounded, expiring package on a trusted encrypted device + sync that never duplicates a transaction on replay + stale reference data halts sensitive operations + clinical or financial conflicts are never auto-resolved",
        endpoint: "/api/offline/conflicts/open",
        envelopeKey: "conflicts",
        columns: [
          { key: "domain", ar: "المجال", en: "Domain", status: true },
          { key: "localSummary", ar: "المحلي", en: "Local" },
          { key: "serverSummary", ar: "المركزي", en: "Server" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
        demoRows: [{ "id": "os1", "domain": "clinical", "localSummary": "جرعة محلية", "serverSummary": "جرعة مركزية مختلفة", "status": "open" }],
      }}
    />
  );
}
