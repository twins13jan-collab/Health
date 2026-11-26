import ModuleScreen from "@/components/ModuleScreen";

export default function Page() {
  return (
    <ModuleScreen
      config={{
        titleKey: "apiManagement",
        subtitleAr: "سجل مركزي لواجهات وإصدارات ومالكين بمصادقة وتفويض وحصص ومفاتيح قابلة للإلغاء الفوري + مخططات رسائل وتوافق خلفي: إصدار كاسر لا يحل محل الحي قبل نافذة إهمال لسابقه + بوابة مطورين وبيئات اختبار وقياس استخدام وأخطاء وتكلفة",
        subtitleEn: "A central register of APIs, versions and owners with authentication, authorization, quotas and instantly revocable keys + message schemas and backward compatibility: a breaking version never replaces the live one before a deprecation window for its predecessor + a developer portal, test environments and usage, error and cost measurement",
        endpoint: "/api/api-management/products",
        envelopeKey: "products",
        columns: [
          { key: "name", ar: "المنتج", en: "Product" },
          { key: "ownerRef", ar: "المالك", en: "Owner" },
          { key: "currentVersion", ar: "الإصدار الحالي", en: "Current version" },
        ],
      }}
    />
  );
}
