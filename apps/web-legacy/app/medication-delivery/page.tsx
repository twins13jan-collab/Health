import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "medicationDelivery",
        subtitleAr: "تحقق من أهلية التوصيل والوصفة ومراجعة واعتماد الصيدلي قبل التجهيز والإرسال + تسليم خاص بهوية متحقق منها وكسر التبريد يبطل التسليم + محاولتا فشل تفتحان مسار الإرجاع والعبوة منكسرة التبريد تُحجر ولا تعود للرف أبدًا",
        subtitleEn: "Delivery eligibility and prescription verified, pharmacist review and approval before preparation and dispatch + special-handling handover only against verified identity and a cold-chain breach voids the delivery + two failed attempts open the return workflow and a breached pack is quarantined, never restocked",
        endpoint: "/api/medication-delivery/orders",
        envelopeKey: "orders",
        columns: [
          { key: "medicationName", ar: "الدواء", en: "Medication" },
          { key: "patientRef", ar: "المريض", en: "Patient" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
