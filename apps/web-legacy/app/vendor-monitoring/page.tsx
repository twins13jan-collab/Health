import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "vendorMonitoring",
        subtitleAr: "تقييم اعتماد على مورد واحد وإدارة موردين بدلاء ومصادر توريد + متابعة التزام بمواعيد تسليم وجودة: التقييم يُحسب من التسليمات (التزام أقل من 90% أو جودة أقل من 80 علم خطر) لا من العلاقات + رصد انقطاع تصنيع ونقل واستيراد وخطط طوارئ وإعادة تخصيص + توثيق قرارات شراء استثنائي ومراجعتها بعد الواقعة",
        subtitleEn: "Single-vendor dependence assessed and alternative vendors and supply sources managed + delivery-date and quality compliance tracked: standing is computed from deliveries (on-time under 90% or quality under 80 flags risk), not from relationships + manufacturing, transport and import disruption monitored, emergency plans and reallocation + exceptional-purchase decisions documented and reviewed after the fact",
        endpoint: "/api/vendor-monitoring/performance",
        envelopeKey: "performances",
        columns: [
          { key: "vendorRef", ar: "المورد", en: "Vendor" },
          { key: "onTimePercent", ar: "الالتزام بالمواعيد %", en: "On-time %" },
          { key: "qualityScore", ar: "درجة الجودة", en: "Quality score" },
        ],
        createForm: {
          endpoint: "/api/vendor-monitoring/performance",
          submitAr: "تسجيل أداء", submitEn: "Record performance",
          fields: [
            { key: "vendorRef", ar: "المورد", en: "Vendor" },
            { key: "onTimePercent", ar: "الالتزام بالمواعيد %", en: "On-time %" },
            { key: "qualityScore", ar: "درجة الجودة", en: "Quality score" },
          ],
        },
      }}
    />
  );
}
