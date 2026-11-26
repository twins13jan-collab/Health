import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "connectedDevices",
        subtitleAr: "سجل أجهزة بمصدر قياس ومريض مرتبط + استقبال قراءات بتحقق من وحدة ووقت وجهاز + جودة إشارة وقيم شاذة وتكرار + معايرة وصلاحية: قراءة جهاز منتهي المعايرة تُحفظ موسومة غير موثوقة ولا تطلق تنبيهًا أبدًا + تنبيهات بحدود معتمدة وحفظ قراءات خام وتحويلاتها ومصدرها",
        subtitleEn: "A device registry with measurement source and linked patient + readings received with unit, time and device verified + signal quality, anomalous values and duplicates + calibration and validity: a reading from an overdue device is preserved, flagged unreliable, and never fires an alert + alerts on approved thresholds and raw readings, conversions and provenance preserved",
        endpoint: "/api/connected-devices/devices",
        envelopeKey: "devices",
        columns: [
          { key: "deviceRef", ar: "الجهاز", en: "Device" },
          { key: "patientRef", ar: "المريض", en: "Patient" },
          { key: "calibrationDue", ar: "المعايرة حتى", en: "Calibration due" },
        ],
      }}
    />
  );
}
