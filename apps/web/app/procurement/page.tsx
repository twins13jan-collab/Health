import ModuleScreen from "@/components/ModuleScreen";

export default function Page() {
  return (
    <ModuleScreen
      config={{
        titleKey: "procurement",
        subtitleAr: "أوامر الشراء والتوريد: المورّد والصنف والكمية والسعر وحالة الأمر من المسودة حتى الاستلام الكامل — الاعتماد والاستلام خطوات موثقة بفاعلها",
        subtitleEn: "Purchase orders: supplier, item, quantity, price and the order's state from draft to full receipt — approval and receiving are documented, attributed steps",
        endpoint: "/api/procurement/orders",
        envelopeKey: "orders",
        columns: [
          { key: "supplier", ar: "المورّد", en: "Supplier" },
          { key: "itemCode", ar: "كود الصنف", en: "Item code" },
          { key: "quantity", ar: "الكمية", en: "Quantity" },
          { key: "unitPrice", ar: "سعر الوحدة", en: "Unit price" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
