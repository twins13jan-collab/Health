import ModuleScreen from "@/components/ModuleScreen";

export default function Page() {
  return (
    <ModuleScreen
      config={{
        titleKey: "transferSafety",
        subtitleAr: "تحقق من استقرار المريض قبل النقل + مستوى مرافقة وأجهزة وأدوية محددة + لا مغادرة قبل قبول الجهة المستقبلة وتأكيد قدرتها + مريض غير مستقر بمرافقة طبيب أو فريق عناية فقط + اكتمال النقل بتسليم موثق عند الوصول لشخص مسمى",
        subtitleEn: "Pre-transfer stability verified + escort level, equipment and medications specified + no departure before the receiving facility accepts and its capacity is confirmed + an unstable patient travels with a physician or ICU team only + the transfer completes at documented handover to a named person on arrival",
        endpoint: "/api/transfer-safety/assessments",
        envelopeKey: "assessments",
        columns: [
          { key: "patientRef", ar: "المريض", en: "Patient" },
          { key: "receivingFacility", ar: "الجهة المستقبلة", en: "Receiving facility" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
