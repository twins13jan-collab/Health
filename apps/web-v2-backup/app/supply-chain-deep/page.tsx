import ModuleScreen from "@/components/ModuleScreen";

export default function Page() {
  return (
    <ModuleScreen
      config={{
        titleKey: "supplyChainDeep",
        subtitleAr: "موردون ومصنعون وموزعون ومصادر توريد باعتماد تنظيمي + تتبع أوامر شراء وشحنات واستلام وتخزين وتوزيع ومرتجعات ودرجات حرارة وسلسلة تبريد وتتبع دفعي وتسلسلي + تحليل مخاطر اعتماد على مورد أو مسار واحد: اعتماد 80% فأكثر خطر مسمى لا يُدار إلا ببديل محدد + مخزون طوارئ وأدوية نادرة ومكونات دم وأكسجين بحد أدنى محسوب العجز",
        subtitleEn: "Suppliers, manufacturers, distributors and supply sources under regulatory accreditation + purchase orders, shipments, receipt, storage, distribution and returns tracked, with temperature, cold chain and batch and serial traceability + single-supplier and single-route dependence analyzed: dependence at 80% or more is a named risk, managed only with an identified alternative + emergency stock, rare medicines, blood components, oxygen and critical supplies with a computed floor shortfall",
        endpoint: "/api/supply-chain-deep/sources",
        envelopeKey: "sources",
        columns: [
          { key: "itemRef", ar: "الصنف", en: "Item" },
          { key: "supplierRef", ar: "المورد", en: "Supplier" },
          { key: "sharePercent", ar: "الحصة %", en: "Share %" },
        ],
      }}
    />
  );
}
