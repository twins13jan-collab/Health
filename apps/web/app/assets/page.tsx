import ModuleScreen from "@/components/ModuleScreen";

export default function Page() {
  return (
    <ModuleScreen
      config={{
        titleKey: "assetsOps",
        subtitleAr: "سجل أصول ومعدات المنشأة التشغيلي: الكود والاسم وموعد الصيانة القادمة والحالة — مع تنبيه الاستحقاقات خلال 30 يومًا من نفس السجل",
        subtitleEn: "The facility's operational asset register: code, name, next maintenance due and status — with the 30-day due view served from the same register",
        endpoint: "/api/assets",
        envelopeKey: "assets",
        columns: [
          { key: "assetCode", ar: "كود الأصل", en: "Asset code" },
          { key: "name", ar: "الاسم", en: "Name" },
          { key: "nextMaintenance", ar: "الصيانة القادمة", en: "Next maintenance" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
