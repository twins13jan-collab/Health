import ModuleScreen from "@/components/ModuleScreen";

export default function Page() {
  return (
    <ModuleScreen
      config={{
        titleKey: "eligibility",
        subtitleAr: "التحقق قبل الحجز من وجود الخدمة فعليًا: معروضة ومرخصة وبسعة وكوادر ومعدات ومستهلكات + التحضير الناقص شرط ظاهر لا منع صامت + التغطية المالية تتحقق وتُبلَّغ لكنها لا تدخل قرار الأهلية أبدًا + كل قيد مسمى بسبب عدم الإتاحة ووقت التحقق ومصدره مسجلان",
        subtitleEn: "Pre-booking verification that the service really exists: offered, licensed, with capacity, staff, equipment and consumables + unmet preparation is a visible condition, not a silent block + coverage is verified and reported but never enters the eligibility decision + every restriction is named, with check time and source recorded",
        endpoint: "/api/eligibility/checks",
        envelopeKey: "checks",
        columns: [
          { key: "serviceName", ar: "الخدمة", en: "Service" },
          { key: "result", ar: "النتيجة", en: "Result" },
          { key: "restrictions", ar: "القيود", en: "Restrictions" },
          { key: "coverageStatus", ar: "التغطية", en: "Coverage" },
        ],
      }}
    />
  );
}
