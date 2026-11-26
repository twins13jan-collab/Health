import ModuleScreen from "@/components/ModuleScreen";

export default function Page() {
  return (
    <ModuleScreen
      config={{
        titleKey: "healthEquity",
        subtitleAr: "قياس توزيع الخدمات والموارد مقابل السكان والاحتياج والعبء المرضي معًا — منطقة بضعف العبء تحتاج ضعف الخدمة لتتساوى + تحليل فجوات وصول بموقع وعمر ونوع وإعاقة وعوامل اجتماعية بعينات آمنة الخصوصية (دون 30 تُحجب) + برامج تغطية لمناطق نائية ومحرومة وعيادات متنقلة لا تُغلق قبل قياس يُظهر الفجوة عند هدفها",
        subtitleEn: "Distribution of services and resources measured against population, need and disease burden together — a region with twice the burden needs twice the service to be equal + access-gap analysis by location, age, gender, disability and social factors on privacy-safe samples (under 30 suppressed) + coverage programmes for remote and deprived areas with mobile clinics that close only when a fresh measurement shows the gap at target",
        endpoint: "/api/health-equity/measurements",
        envelopeKey: "measurements",
        columns: [
          { key: "region", ar: "المنطقة", en: "Region" },
          { key: "population", ar: "السكان", en: "Population" },
          { key: "serviceUnits", ar: "وحدات الخدمة", en: "Service units" },
        ],
      }}
    />
  );
}
