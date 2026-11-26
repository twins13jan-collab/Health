import ModuleScreen from "@/components/ModuleScreen";

export default function Page() {
  return (
    <ModuleScreen
      config={{
        titleKey: "workforceHealth",
        subtitleAr: "فحوص دورية ولياقة مهنية وتطعيمات للعاملين الصحيين تنتهي صلاحيتها فتنتهي اللياقة + تعرضات دم وعدوى وكيميائيات وإشعاع وغازات تُحقق وتُتابع قبل إغلاق ملفها + معدات وقاية بتدريب مسجل — بلا تدريب زي لا حماية + دعم نفسي واحتراق بسرية خلف جدران أدوار",
        subtitleEn: "Periodic examinations, occupational fitness and vaccinations for health workers that expire — and take fitness with them + blood, infection, chemical, radiation and gas exposures investigated and followed up before their files close + PPE issued with recorded training — without training it is costume, not protection + psychological support and burnout care confidential behind role walls",
        endpoint: "/api/workforce-health/records",
        envelopeKey: "records",
        columns: [
          { key: "workerRef", ar: "العامل", en: "Worker" },
          { key: "workerRole", ar: "الدور", en: "Role" },
          { key: "lastPeriodicExamDate", ar: "آخر فحص دوري", en: "Last periodic exam" },
        ],
      }}
    />
  );
}
