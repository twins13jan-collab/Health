import ModuleScreen from "@/components/ModuleScreen";

export default function Page() {
  return (
    <ModuleScreen
      config={{
        titleKey: "inventory",
        subtitleAr: "أصناف ومخزون بحركات مسببة ورفض الرصيد السالب وتنبيه إعادة الطلب",
        subtitleEn: "Stock with reasoned movements, negative balance rejected, reorder alerts",
        endpoint: "/api/inventory/items",
        envelopeKey: "items",
        columns: [
          { key: "itemCode", ar: "رمز الصنف", en: "Item code" },
          { key: "name", ar: "الصنف", en: "Item" },
          { key: "quantity", ar: "الرصيد", en: "Qty" },
          { key: "reorderLevel", ar: "حد الطلب", en: "Reorder level" },
        ],
      }}
    />
  );
}
