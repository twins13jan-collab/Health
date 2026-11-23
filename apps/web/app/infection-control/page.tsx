import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "infectionControl",
        subtitleAr: "حالات مشتبه بها ومؤكدة مبلّغ عنها — مؤكدة واجبة الإبلاغ دون إشعار تعد إخلالًا + العزل الهوائي بضغط سلبي فقط وإنهاء العزل لفريق مكافحة العدوى وحده + مؤشرات عدوى الرعاية محسوبة لكل 1000 يوم جهاز + ترشيد المضادات مرتبط بنتائج المزارع",
        subtitleEn: "Suspected and confirmed cases reported — a confirmed notifiable case without notification is a breach + airborne isolation only in negative-pressure rooms and only infection control discontinues isolation + HAI indicators computed per 1,000 device-days + antimicrobial stewardship tied to culture results",
        endpoint: "/api/infection-control/cases",
        envelopeKey: "cases",
        columns: [
          { key: "organism", ar: "الميكروب", en: "Organism" },
          { key: "caseType", ar: "النوع", en: "Type" },
          { key: "source", ar: "المصدر", en: "Source" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
        demoRows: [{ "id": "ip1", "organism": "MRSA", "caseType": "confirmed", "source": "hai", "status": "isolated" }],
      }}
    />
  );
}
