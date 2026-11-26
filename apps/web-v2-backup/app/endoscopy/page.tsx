import ModuleScreen from "@/components/ModuleScreen";

export default function Page() {
  return (
    <ModuleScreen
      config={{
        titleKey: "endoscopy",
        subtitleAr: "منظار غير معاد معالجته بالكامل لا يُحجز وفشل اختبار التسرب يحجره مع تتبع حالاته + لا بدء بلا قائمة تحقق مكتملة + عينة بلا ملصق موثوق تُعزل ولا تُنسب + خروج بتقييم إفاقة",
        subtitleEn: "A scope not fully reprocessed cannot be booked and a failed leak test quarantines it with its cases traceable + no start without a complete checklist + an unlabeled specimen is isolated, never attributed + discharge by recovery score",
        endpoint: "/api/endoscopy/requests?patientId=00000000-0000-0000-0000-000000000000",
        envelopeKey: "requests",
        columns: [
          { key: "scopeType", ar: "نوع المنظار", en: "Scope type" },
          { key: "indication", ar: "الاستطباب", en: "Indication" },
          { key: "urgency", ar: "الاستعجال", en: "Urgency", status: true },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
