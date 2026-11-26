import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "rehab",
        subtitleAr: "تأهيل بقياس حقيقي: لا جلسات دون تقييم أساسي وخطة معتمدة + ألم حاد أو تدهور يوقف الجلسة ويصعّد + التحسن يُثبت بقياس نتيجة موثق النسخة لا بعدد الجلسات",
        subtitleEn: "Rehabilitation with real measurement: no sessions without a baseline assessment and an approved plan + acute pain or deterioration stops and escalates + improvement is proven by versioned outcome measures, never session counts",
        endpoint: "/api/rehab/referrals?patientId=00000000-0000-0000-0000-000000000000",
        envelopeKey: "referrals",
        columns: [
          { key: "reason", ar: "سبب الإحالة", en: "Reason" },
          { key: "referringPhysician", ar: "الطبيب المحيل", en: "Referring physician" },
          { key: "createdAt", ar: "التاريخ", en: "Date" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
