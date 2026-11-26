import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "preventive",
        subtitleAr: "برامج فحص جماعي بدعوات وموافقات ومواعيد للمؤهلين فقط + نتيجة إيجابية ليست تشخيصًا: تفتح مسارًا تأكيديًا وإحالة لا يُغلق قبل اكتمالهما + متابعة عدم الحضور وإعادة دعوة + تغطية محسوبة من الحضور الفعلي",
        subtitleEn: "Mass screening programmes with invitations, consents and appointments for the eligible only + a positive screen is not a diagnosis: it opens a confirmatory-and-referral pathway that closes only through both + no-shows followed up and re-invited + coverage computed from real attendance",
        endpoint: "/api/preventive/programs",
        envelopeKey: "programs",
        columns: [
          { key: "name", ar: "البرنامج", en: "Programme" },
          { key: "targetCondition", ar: "الحالة المستهدفة", en: "Target condition" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
