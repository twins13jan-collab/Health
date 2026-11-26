import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "telemedicine",
        subtitleAr: "حجز جلسة وتحقق من الطرفين وموافقة + مشاركة ملفات وقياسات + توثيق حدود الفحص عن بعد وقرار الحضور: رعاية تدعي بلا حدود هي الخطر + وصفات وإحالات وفق اللوائح كأي وصفة + جودة اتصال وانقطاع وحماية جلسة ومنع تسجيل غير مصرح + ربط أجهزة منزلية بتحقق من مصدر القياس",
        subtitleEn: "Session booking with both parties verified and consent obtained + files and measurements shared + the limits of remote examination and the in-person decision documented: care that pretends it has no limits is the hazard + prescriptions and referrals under the same regulations as any prescription + connection quality and dropouts, session protection and unauthorized-recording prevention + home devices linked with verified measurement sources",
        endpoint: "/api/telemedicine/sessions",
        envelopeKey: "sessions",
        columns: [
          { key: "patientRef", ar: "المريض", en: "Patient" },
          { key: "clinicianRef", ar: "الطبيب", en: "Clinician" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
