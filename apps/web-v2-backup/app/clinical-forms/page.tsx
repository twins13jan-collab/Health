import ModuleScreen from "@/components/ModuleScreen";

export default function Page() {
  return (
    <ModuleScreen
      config={{
        titleKey: "clinicalForms",
        subtitleAr: "قوالب تقييم وموافقات وتقارير بحقول حسب التخصص والخدمة وإصدارات مدارة + كتابة على الإصدار النشط فقط وكاتب وتوقيع وتوقيت مسجل + نموذج موقع غير قابل للتعديل: تصحيحه نموذج جديد يحل محله ويظهر الاثنان + حفظ نهائي للموقع فقط وتحويل لبيانات منظمة",
        subtitleEn: "Assessment, consent and report templates with fields per specialty and service and managed versions + writing only on the active version with author, signature and timing recorded + a signed form is immutable: its correction is a new instance that supersedes it and both remain visible + final filing for signed forms only, converted to structured data",
        endpoint: "/api/clinical-forms/templates",
        envelopeKey: "templates",
        columns: [
          { key: "name", ar: "القالب", en: "Template" },
          { key: "specialty", ar: "التخصص", en: "Specialty" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
