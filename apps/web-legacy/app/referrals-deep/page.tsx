import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "referralsDeep",
        subtitleAr: "دليل مواعيد موحد حسب خدمة وقدرة فعلية + إحالة بسبب سريري ومرفقات وأولوية + قبول أو رفض بسبب وبديل معًا: رفض بلا بديل طريق مسدود بثوب إجابة + قوائم انتظار بقاعدة عدالة معلنة (أولوية سريرية ثم أقدمية انتظار) تُحسب علنًا لا بمن اتصل بمن + حجز وإلغاء وتأكيد وتذكير وإحالة عكسية وقياس زمن إحالة وإتمام مسار",
        subtitleEn: "A unified appointment directory by service and real capacity + referrals with clinical reason, attachments and priority + acceptance, or rejection with a reason AND an alternative: a rejection without an alternative is a dead end dressed as an answer + waiting lists under a declared fairness rule (clinical priority, then longest waiting) computed in the open, never by who called whom + booking, cancellation, confirmation, reminders, reverse referrals, and referral-time and pathway-completion measurement",
        endpoint: "/api/referrals-deep/referrals",
        envelopeKey: "referrals",
        columns: [
          { key: "patientRef", ar: "المريض", en: "Patient" },
          { key: "clinicalReason", ar: "السبب السريري", en: "Clinical reason" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
