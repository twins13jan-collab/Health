import ModuleScreen from "@/components/ModuleScreen";

export default function Page() {
  return (
    <ModuleScreen
      config={{
        titleKey: "continuity",
        subtitleAr: "تحليل أثر توقف الخدمات وأهداف زمن ونقطة استعادة معلنة + نسخ احتياطي مهيأ ليس دليلًا: الاستعداد يُثبت باختبار استعادة فعلي حقق أهدافه خلال 6 أشهر + تشغيل بديل عند فقد الشبكة أو مركز البيانات بفعل معلن وسبب موثق",
        subtitleEn: "Service-outage impact analysis with declared recovery-time and recovery-point objectives + a configured backup is not evidence: readiness is proven only by an actual restore test that met its targets within 6 months + alternate operations on network or data-centre loss as a declared act with a documented reason",
        endpoint: "/api/continuity/services",
        envelopeKey: "services",
        columns: [
          { key: "name", ar: "الخدمة", en: "Service" },
          { key: "impactLevel", ar: "أثر التوقف", en: "Outage impact" },
          { key: "rtoMinutes", ar: "هدف الاستعادة (د)", en: "RTO (min)" },
        ],
      }}
    />
  );
}
