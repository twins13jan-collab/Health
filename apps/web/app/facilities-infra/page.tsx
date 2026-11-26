import ModuleScreen from "@/components/ModuleScreen";

export default function Page() {
  return (
    <ModuleScreen
      config={{
        titleKey: "facilitiesInfra",
        subtitleAr: "مبانٍ وغرف وأنظمة كهروميكانيكية وغازات طبية بمراقبة طاقة ومياه ووقود وتبريد وتهوية وجودة هواء وضغط وحرارة + صيانة وأوامر عمل: أمر حرج لنظام حرج لا يُغلق بلا خطة بديل قائمة وقراءة ما بعد إصلاح داخل الحدود — فالإصلاح الورقي لا يعيد الأكسجين",
        subtitleEn: "Buildings, rooms, electromechanical systems and medical gases with energy, water, fuel, cooling, ventilation, air quality, pressure and temperature monitoring + maintenance and work orders: a critical order on a critical system closes only with a fallback plan in place and a post-repair reading within limits — a paper repair does not restore oxygen",
        endpoint: "/api/facilities-infra/systems",
        envelopeKey: "systems",
        columns: [
          { key: "name", ar: "النظام", en: "System" },
          { key: "systemType", ar: "النوع", en: "Type" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
