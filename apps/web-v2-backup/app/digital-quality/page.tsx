import ModuleScreen from "@/components/ModuleScreen";

export default function Page() {
  return (
    <ModuleScreen
      config={{
        titleKey: "digitalQuality",
        subtitleAr: "قياس إتمام المهام والأخطاء وزمن التحميل والاستقرار واتصال الواجهات بالـAPI مقابل بيانات فعلية + القاعدة الجوهرية: البيانات التجريبية لا تظهر كتشغيلية أبدًا — سطح إنتاجي بحادث بيانات تجريبية مفتوح غير ممتثل حتى يُحل + اختبار الواجهات مع مستخدمين فعليين",
        subtitleEn: "Task completion, errors, load time and stability measured, with interface-to-API connectivity verified against real data + the cardinal rule: demonstration data never appears as operational data — a production surface with an open demo-data incident is non-compliant until resolved + interfaces tested with actual users",
        endpoint: "/api/digital-quality/checks",
        envelopeKey: "checks",
        columns: [
          { key: "surface", ar: "السطح", en: "Surface" },
          { key: "environment", ar: "البيئة", en: "Env" },
          { key: "passed", ar: "ناجح", en: "Passed" },
        ],
      }}
    />
  );
}
