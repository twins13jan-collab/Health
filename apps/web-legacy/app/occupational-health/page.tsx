import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "occupationalHealth",
        subtitleAr: "برامج فحص ومراقبة مهنية للعاملين بالقطاعات عالية الخطورة: تعرضات مواد وضوضاء وحرارة وإشعاع + اللياقة محسوبة: فحص دوري متأخر أو حادث تعرض بلا فحص لاحق يزيل التصريح + تقرير جهة العمل بحدود إفصاح — لياقة وقيود فقط، التشخيص لا يخرج أبدًا",
        subtitleEn: "Occupational screening and surveillance for high-risk sector workers: substance, noise, heat and radiation exposures + clearance is computed: an overdue periodic exam or an exposure incident without its follow-up exam removes clearance + employer reports observe disclosure limits — fitness and restrictions only, the diagnosis never leaves",
        endpoint: "/api/occupational-health/workers",
        envelopeKey: "workers",
        columns: [
          { key: "workerCode", ar: "كود العامل", en: "Worker Code" },
          { key: "sector", ar: "القطاع", en: "Sector" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
