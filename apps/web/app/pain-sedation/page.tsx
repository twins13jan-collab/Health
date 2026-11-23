import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "painSedation",
        subtitleAr: "تقييم ألم بمقاييس مناسبة للعمر والحالة (FLACC لغير الناطق ووجوه للأطفال ورقمي للبالغين) + خطط تسكين حادة ومزمنة وبعد عمليات وتلطيفية بأدوية خاضعة للرقابة مسجلة + تهدئة إجرائية خارج العمليات لا تبدأ قبل تقييم ومجرى هواء وموافقة + خروج بدرجة إفاقة Aldrete لا تقل عن 9",
        subtitleEn: "Pain assessed with age-appropriate scales (FLACC for the preverbal, faces for children, numeric for adults) + acute, chronic, postoperative and palliative analgesia plans with controlled medications registered + procedural sedation outside theatre starts only after assessment, airway and consent + discharge at an Aldrete recovery score of 9 or more",
        endpoint: "/api/pain-sedation/pain-assessments",
        envelopeKey: "assessments",
        columns: [
          { key: "patientRef", ar: "المريض", en: "Patient" },
          { key: "scale", ar: "المقياس", en: "Scale" },
          { key: "score", ar: "الدرجة", en: "Score" },
        ],
        demoRows: [{ "id": "ps1", "patientRef": "P-801", "scale": "NRS", "score": 8 }],
      }}
    />
  );
}
