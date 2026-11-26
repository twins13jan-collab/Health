import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "porterage",
        subtitleAr: "نقل المرضى داخل المنشأة بأولويات: الحرج ومعتمد الأكسجين لا يتحرك دون مرافق سريري مسمى + زمن استجابة مقاس بهدف لكل أولوية والخرق معلّم + تجهيز الغرفة بعد الخروج لا يكتمل إلا بتنظيف وسرير وتفتيش موقع + فقد الغسيل محسوب لا ممتص بصمت + طلبات المرضى الفندقية متعقبة حتى التنفيذ",
        subtitleEn: "In-facility patient transport by priority: critical and oxygen-dependent patients never move without a named clinical escort + response time measured per priority with breaches flagged + post-discharge room turnover completes only with cleaning, bed and a signed inspection + laundry losses counted openly + patient hotel requests tracked to fulfilment",
        endpoint: "/api/porterage/transports",
        envelopeKey: "transports",
        columns: [
          { key: "patientRef", ar: "المريض", en: "Patient" },
          { key: "priority", ar: "الأولوية", en: "Priority" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
