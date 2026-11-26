import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "amrDeep",
        subtitleAr: "تجميع بيانات مزارع وحساسية بتعريفات موحدة لجراثيم ومضادات وطرق اختبار + تحليل مقاومة حسب منشأة ومنطقة وعينة وربط استهلاك بنتائج — والمعدل من نتائج موحدة الطريقة فقط، ونمط غير معتاد يقفز فوق خط الأساس يتحقق قبل نشره، وتقارير الترصد محمية الهوية دائمًا",
        subtitleEn: "Culture and susceptibility data aggregated under unified organism, antibiotic and test-method definitions + resistance analyzed by facility, region and specimen, with consumption linked to outcomes — a rate comes from standardized-method results only, an unusual pattern jumping over baseline is verified before it is published, and surveillance reports are always identity-protected",
        endpoint: "/api/amr-deep/cultures",
        envelopeKey: "cultures",
        columns: [
          { key: "organism", ar: "الجرثومة", en: "Organism" },
          { key: "antibiotic", ar: "المضاد", en: "Antibiotic" },
          { key: "result", ar: "النتيجة", en: "Result" },
        ],
      }}
    />
  );
}
