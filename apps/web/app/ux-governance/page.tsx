import ModuleScreen from "@/components/ModuleScreen";

export default function Page() {
  return (
    <ModuleScreen
      config={{
        titleKey: "uxGovernance",
        subtitleAr: "أدوار مستخدمين وسيناريوهات عمل مُدارة ضد مكتبة تصميم موحدة + المهام الحرجة بهدف زمني مقاس وبطؤها ملاحظة سلامة لا ذوق + اختبارات سهولة استخدام موثقة + لا اعتماد لسيناريو دون جلسة مكتملة وملاحظات حاجبة مفتوحة صفر",
        subtitleEn: "User roles and work scenarios managed against a unified design library + critical tasks carry measured time targets and slowness is a safety finding, not a taste issue + documented usability testing + no scenario validates without a completed session and zero open blocker findings",
        endpoint: "/api/ux-governance/scenarios",
        envelopeKey: "scenarios",
        columns: [
          { key: "scenarioName", ar: "السيناريو", en: "Scenario" },
          { key: "userRole", ar: "الدور", en: "Role" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
