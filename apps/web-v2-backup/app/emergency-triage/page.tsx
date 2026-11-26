import ModuleScreen from "@/components/ModuleScreen";

export default function Page() {
  return (
    <ModuleScreen
      config={{
        titleKey: "emergencyTriage",
        subtitleAr: "وصول وفرز ومستوى خطورة وإعادة تقييم + مسارات إنعاش وإصابات وحروق وتسمم وسكتة وجلطات + أوامر طوارئ واستشارات وتسليم موثق — وزمن الباب إلى الطبيب وعد يقاس بحسب الخطورة لا يُروى من الذاكرة، والمغادرة قبل اكتمال التقييم حق للمريض بعد شرح المخاطر وتوقيعه: خروج غير موثق مغادرة غير آمنة لا إغلاق نظيف",
        subtitleEn: "Arrival, triage, acuity level and re-evaluation + resuscitation, trauma, burns, poisoning, stroke and MI pathways + emergency orders, consultations and documented handovers — door-to-doctor time is a promise measured by acuity, not remembered, and leaving before assessment completes is the patient's right after the risks are explained and signed: an undocumented walk-out is an unsafe departure, never a clean closure",
        endpoint: "/api/emergency-triage/assessments",
        envelopeKey: "assessments",
        columns: [
          { key: "patientRef", ar: "المريض", en: "Patient" },
          { key: "esiLevel", ar: "مستوى الخطورة", en: "ESI level" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
