import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "labs",
        subtitleAr: "طلبات المختبر ومسارها التسلسلي — النتائج الحرجة بتنبيه وإقرار إلزامي (دورة 219)",
        subtitleEn: "Lab orders on their sequential path — critical results alerted with mandatory acknowledgement (cycle 219)",
        endpoint: "/api/labs/orders",
        envelopeKey: "orders",
        columns: [
          { key: "testCode", ar: "رمز الفحص", en: "Test code" },
          { key: "testName", ar: "اسم الفحص", en: "Test name" },
          { key: "priority", ar: "الأولوية", en: "Priority" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
        demoRows: [{"id": "d1", "testCode": "K", "testName": "Potassium", "priority": "stat", "status": "resulted"}, {"id": "d2", "testCode": "GLU", "testName": "Glucose", "priority": "routine", "status": "verified"}],
      }}
    />
  );
}
