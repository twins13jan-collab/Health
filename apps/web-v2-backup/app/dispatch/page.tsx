import ModuleScreen from "@/components/ModuleScreen";

export default function Page() {
  return (
    <ModuleScreen
      config={{
        titleKey: "dispatch",
        subtitleAr: "طلبات إسعاف بأقرب وحدة متاحة بالقدرة المطلوبة وقائمة انتظار وتصعيد (دورة 224)",
        subtitleEn: "Ambulance requests assigned to the nearest capable available unit, with queue and escalation (cycle 224)",
        endpoint: "/api/dispatch/queue",
        envelopeKey: "queue",
        columns: [
          { key: "complaint", ar: "الشكوى", en: "Complaint" },
          { key: "requiredCapability", ar: "القدرة المطلوبة", en: "Capability" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
