import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "capabilityAccess",
        subtitleAr: "ربط خدمة بكوادر وأجهزة وتراخيص وبروتوكولات + شروط تفعيل وإيقاف مؤقت وتحقق دوري من استمرار القدرة + مستويات إحالة ومراكز تميز — والخدمة لا تُعلن متاحة لمجرد وجود اسمها: كوادر وتجهيز وترخيص وبروتوكول وتحقق حديث (30 يومًا) معًا، وعند عدم الإتاحة يظهر السبب وآخر تحقق لا يُخفى",
        subtitleEn: "A service linked to its staff, equipment, licences and protocols + activation and temporary-suspension conditions and periodic verification that the capability continues + referral levels and centres of excellence — a service is never announced available merely because its name exists: staff, equipment, licence, protocol and fresh verification (30 days) together, and when unavailable the reason and last verification are shown, never hidden",
        endpoint: "/api/capability-access/capabilities",
        envelopeKey: "capabilities",
        columns: [
          { key: "serviceRef", ar: "الخدمة", en: "Service" },
          { key: "staffReady", ar: "كوادر جاهزة", en: "Staff ready" },
          { key: "equipmentReady", ar: "تجهيز جاهز", en: "Equipment ready" },
        ],
      }}
    />
  );
}
