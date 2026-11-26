import ModuleScreen from "@/components/ModuleScreen";

export default function Page() {
  return (
    <ModuleScreen
      config={{
        titleKey: "multilingual",
        subtitleAr: "عربية باتجاه RTL وإنجليزية + مصطلحات وترجمات طبية لا تُنشر قبل مراجعة طبية: تعليمات جرعة مترجمة آليًا خطر سريري + توافق قارئات شاشة ولوحة مفاتيح وتباين وخطوط وواجهات ميسرة ودعم أجهزة محدودة واتصال ضعيف — وادعاء الإتاحة يقف فقط بفحص ناجح واختبار مع فئات متنوعة من المستخدمين أنفسهم",
        subtitleEn: "Arabic with RTL direction and English + medical terms and translations published only after medical review: a machine-translated dosage instruction is a clinical hazard + screen-reader and keyboard compatibility, contrast, fonts and accessible interfaces, low-capability devices and weak connectivity — and an accessibility claim stands only on a passed check tested with diverse users themselves",
        endpoint: "/api/multilingual/translations",
        envelopeKey: "translations",
        columns: [
          { key: "termKey", ar: "المصطلح", en: "Term" },
          { key: "language", ar: "اللغة", en: "Language" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
