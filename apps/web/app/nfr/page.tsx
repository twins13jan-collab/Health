import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "nfr",
        subtitleAr: "تعريف أداء وتوسع وتوافر ومرونة وخصوصية وأمن وتدقيق واحتفاظ وتوافق وقابلية نقل وسهولة استخدام وعمل دون اتصال وحدود تحميل وملفات ورسائل لكل خدمة — والمتطلب لا يُستوفى إلا بقياس حديث داخل هدفه: غير المقاس غير موثق، وخدمة بمتطلب حرج غير مستوفٍ ليست جاهزة مهما اكتملت مزاياها",
        subtitleEn: "Performance, scalability, availability, resilience, privacy, security, audit, retention, compatibility, portability, usability, offline operation and load, file and message limits defined per service — and a requirement is met only by a fresh measurement within its target: unmeasured means unverified, and a service with an unmet critical requirement is not ready however complete its features",
        endpoint: "/api/nfr/requirements",
        envelopeKey: "requirements",
        columns: [
          { key: "serviceRef", ar: "الخدمة", en: "Service" },
          { key: "category", ar: "الفئة", en: "Category" },
          { key: "metric", ar: "المقياس", en: "Metric" },
        ],
        demoRows: [{ "id": "nf1", "serviceRef": "خدمة الطوارئ", "category": "availability", "metric": "زمن التوافر الشهري %" }],
      }}
    />
  );
}
