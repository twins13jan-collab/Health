import ModuleScreen from "@/components/ModuleScreen";

export default function Page() {
  return (
    <ModuleScreen
      config={{
        titleKey: "integrations",
        subtitleAr: "كل رسالة تتبع حتى ACK أو فشل نهائي وHTTP 200 ليس إثبات معنى + تكرار يمنع بمفتاح عدم التكرار + رسالة غير صالحة تعزل + فشل يتصاعد إلى رسائل ميتة ومصالحة + إصدار مورد جديد لا يفعل قبل اختبار توافق",
        subtitleEn: "Every message is traced to an ACK or a final failure and HTTP 200 proves neither meaning nor effect + duplicates are stopped by the idempotency key + invalid payloads are quarantined + failures escalate to dead-letter and reconciliation + a new vendor version never activates before a compatibility test",
        endpoint: "/api/integrations/messages/quarantined",
        envelopeKey: "messages",
        columns: [
          { key: "messageId", ar: "معرف الرسالة", en: "Message ID" },
          { key: "sourceSystem", ar: "المصدر", en: "Source" },
          { key: "destination", ar: "الوجهة", en: "Destination" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
