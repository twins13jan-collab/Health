import ModuleScreen from "@/components/ModuleScreen";

export default function Page() {
  return (
    <ModuleScreen
      config={{
        titleKey: "hazmat",
        subtitleAr: "تصنيف مواد وخطر ونطاق تعرض + مناطق عزل ساخنة ودافئة وباردة + مصاب ملوث لا يدخل منطقة العلاج قبل التطهير + تنسيق واجب مع مراكز السموم والجهات المختصة للحوادث الكيميائية والإشعاعية + لا إغلاق ومعرّض واحد مدين بفحص متابعة",
        subtitleEn: "Agent, hazard and exposure-scope classification + hot, warm and cold isolation zones + a contaminated casualty never enters the treatment zone before decontamination + mandatory coordination with poison centres and competent authorities for chemical and radiological events + no closure while one exposed person still owes a follow-up examination",
        endpoint: "/api/hazmat/incidents",
        envelopeKey: "incidents",
        columns: [
          { key: "agentName", ar: "المادة", en: "Agent" },
          { key: "agentType", ar: "النوع", en: "Type" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
