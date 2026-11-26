import ModuleScreen from "@/components/ModuleScreen";

export default function Page() {
  return (
    <ModuleScreen
      config={{
        titleKey: "clinicalAi",
        subtitleAr: "سجل نماذج بمصدر وقيود معلنة + اعتماد الاستخدام السريري للجنة لا لمالك النموذج + النموذج يمتنع عند مدخلات ناقصة أو خارج نطاق التحقق + لا أثر سريري دون قرار بشري مسجل والممارس صاحب القرار النهائي + الانحراف يعلق النموذج",
        subtitleEn: "A model registry with provenance and declared limits + clinical use is approved by a committee, never the model owner + the model abstains on incomplete or out-of-scope inputs + nothing affects care without a recorded human decision and the practitioner holds the final say + drift suspends the model",
        endpoint: "/api/clinical-ai/models",
        envelopeKey: "models",
        columns: [
          { key: "name", ar: "النموذج", en: "Model" },
          { key: "version", ar: "الإصدار", en: "Version" },
          { key: "riskClass", ar: "فئة الخطورة", en: "Risk class" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
