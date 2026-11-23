import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
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
        demoRows: [{"id": "d1", "itemCode": "SUT-001", "name": "Sutures 3-0", "quantity": 140, "reorderLevel": 50}, {"id": "d2", "itemCode": "IV-SET", "name": "IV set", "quantity": 32, "reorderLevel": 40}],
      }}
    />
  );
}
