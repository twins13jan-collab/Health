import ModuleScreen from "@/components/ModuleScreen";

export default function Page() {
  return (
    <ModuleScreen
      config={{
        titleKey: "openData",
        subtitleAr: "تحديد مجموعات بيانات مسموح نشرها + تقييم مخاطر إعادة تعرف + تجميع وإخفاء هوية بكبت خلايا صغيرة (حد أدنى 10: دونها قد تكون الإحصائية شخصًا مسمى) + توثيق تعريفات ومنهج ومصدر + شروط إعادة استخدام وتحديث + سحب فوري بسبب مسجل عند خطأ أو خطر خصوصية: بيانات لا تُسحب تسريب ببوابة",
        subtitleEn: "The datasets permitted for publication defined + re-identification risk assessed + aggregation and anonymization with small-cell suppression (minimum cell 10: below that, a statistic can be a named person) + definitions, methodology and source documented + reuse terms and updates + immediate withdrawal with a recorded reason on error or privacy risk: data that cannot be withdrawn is a leak with a portal",
        endpoint: "/api/open-data/datasets",
        envelopeKey: "datasets",
        columns: [
          { key: "name", ar: "المجموعة", en: "Dataset" },
          { key: "minCellSize", ar: "حد الخلية الأدنى", en: "Min cell" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
        rowActions: [
          { labelAr: "نشر", labelEn: "Publish", endpoint: "/api/open-data/datasets/{id}/publish" },
        ],
      }}
    />
  );
}
