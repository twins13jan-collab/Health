import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "appointments",
        subtitleAr: "مواعيد بلا حجز مزدوج لنفس الطبيب + استحقاق تطعيمات EPI",
        subtitleEn: "Appointments with no double-booking per doctor + EPI vaccination due checks",
        endpoint: "/api/notifications/mine",
        envelopeKey: "notifications",
        columns: [
          { key: "subject", ar: "الموضوع", en: "Subject" },
          { key: "channel", ar: "القناة", en: "Channel" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
        demoRows: [{"id": "d1", "subject": "تذكير بموعد العيادة", "channel": "sms", "status": "queued"}],
      }}
    />
  );
}
