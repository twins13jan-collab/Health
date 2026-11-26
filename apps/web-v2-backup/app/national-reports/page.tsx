import ModuleScreen from "@/components/ModuleScreen";

export default function Page() {
  return (
    <ModuleScreen
      config={{
        titleKey: "nationalReports",
        subtitleAr: "كل مؤشر قابل لإعادة الحساب وله مالك ومصدر ووقت قطع وتغطية + تأخر منشأة يظهر كنسبة تغطية ولا يعمم الرقم على الدولة + تغيير التعريف إصدار جديد بتحذير مقارنة + أعداد صغيرة مكبوحة",
        subtitleEn: "Every indicator is recomputable with an owner, source, cutoff and coverage + a late facility shows as coverage and the figure is never generalised nationally + definition changes issue new versions with comparability warnings + small counts are suppressed",
        endpoint: "/api/national-reports/public/datasets",
        envelopeKey: "datasets",
        columns: [
          { key: "aggregationLevel", ar: "مستوى التجميع", en: "Aggregation level" },
          { key: "license", ar: "الترخيص", en: "License" },
          { key: "approvedBy", ar: "اعتماد", en: "Approved by" },
          { key: "publishedAt", ar: "نشر في", en: "Published at" },
        ],
      }}
    />
  );
}
