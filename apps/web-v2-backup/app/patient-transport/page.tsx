import ModuleScreen from "@/components/ModuleScreen";

export default function Page() {
  return (
    <ModuleScreen
      config={{
        titleKey: "patientTransport",
        subtitleAr: "طلبات نقل للغسيل والعلاج والتأهيل باحتياجات كرسي أو نقالة أو أكسجين محددة + جدولة بمزودين موثقين ترخيصهم سارٍ يوم الرحلة وقدرتهم تطابق الحاجة + إتمام وتأخير وشكاوى موثقة لكل رحلة — تأخير متكرر نمط يُراجع لا حكاية",
        subtitleEn: "Transport requests for dialysis, therapy and rehabilitation with wheelchair, stretcher and oxygen needs specified + scheduling only with verified providers whose licence covers the trip date and whose capability matches the need + completion, delay and complaints recorded per trip — repeated delay is a reviewable pattern, not an anecdote",
        endpoint: "/api/patient-transport/requests",
        envelopeKey: "requests",
        columns: [
          { key: "patientRef", ar: "المريض", en: "Patient" },
          { key: "purpose", ar: "الغرض", en: "Purpose" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
