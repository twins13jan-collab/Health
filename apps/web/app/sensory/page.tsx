import ModuleScreen from "@/components/ModuleScreen";

export default function Page() {
  return (
    <ModuleScreen
      config={{
        titleKey: "sensory",
        subtitleAr: "كل قياس بجهة ووحدة وجهاز وطريقة — رقم بلا وحدة لا يُخزن + فشل المعايرة يبطل النتيجة ولا يمررها + التصحيح يحفظ الأصل + جهاز مساعد غير ملائم لا يُسلَّم أبدًا",
        subtitleEn: "Every measurement carries side, unit, device and method — a number without a unit is never stored + failed calibration invalidates the result + corrections preserve the original + an unfit assistive device is never delivered",
        endpoint: "/api/sensory/assessments?patientId=00000000-0000-0000-0000-000000000000",
        envelopeKey: "assessments",
        columns: [
          { key: "organ", ar: "العضو", en: "Organ" },
          { key: "side", ar: "الجهة", en: "Side", status: true },
          { key: "measurements", ar: "القياسات", en: "Measurements" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
