import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "mentalHealth",
        subtitleAr: "سرية مشددة: الوصول لفريق العلاج فقط وكل قراءة مسجلة + التشخيص لا يظهر في القوائم أبدًا + خطر وشيك يفعّل بروتوكول السلامة وخطة سلامة إلزامية عند الخطر المرتفع",
        subtitleEn: "Strict confidentiality: treating-team access only with every read audited + diagnosis never appears in list views + imminent risk activates the safety protocol and a safety plan is mandatory at high risk",
        endpoint: "/api/mental-health/episodes?patientId=00000000-0000-0000-0000-000000000000",
        envelopeKey: "episodes",
        columns: [
          { key: "referral_reason", ar: "سبب الإحالة", en: "Referral reason" },
          { key: "urgency", ar: "الاستعجال", en: "Urgency", status: true },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
