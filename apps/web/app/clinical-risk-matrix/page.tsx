import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "clinicalRiskMatrix",
        subtitleAr: "مخاطر هوية ودواء وجرعة وإجراء ونتائج حرجة وتأخير ونقل وتسليم وانقطاع أجهزة — كل خطر بثلاث طبقات ضوابط: منع وكشف واستجابة، ونقص طبقة يعني خطرًا مفتوحًا + مراجعة إنذارات كاذبة وإرهاق تنبيه: تنبيه يخطئ أكثر من نصف المرات على عينة حقيقية يذهب للمراجعة فالإنذار المتجاهل لا يحمي أحدًا",
        subtitleEn: "Hazards of identity, medication, dose, procedure, critical results, delay, transport, handover and device outage — every hazard with three control layers: prevention, detection and response, and a missing layer means an open hazard + false-alarm and alert-fatigue review: an alert wrong more than half the time on a real sample goes under review, because an ignored alarm protects no one",
        endpoint: "/api/clinical-risk-matrix/risks",
        envelopeKey: "risks",
        columns: [
          { key: "title", ar: "الخطر", en: "Hazard" },
          { key: "hazardDomain", ar: "المجال", en: "Domain" },
          { key: "severity", ar: "الشدة", en: "Severity" },
        ],
        demoRows: [{ "id": "mx1", "title": "تشابه أسماء أدوية عالية الخطورة", "hazardDomain": "medication", "severity": 5 }],
      }}
    />
  );
}
