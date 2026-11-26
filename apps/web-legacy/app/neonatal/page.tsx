import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "neonatal",
        subtitleAr: "مواليد مرتبطون بالأم بمعرفات لا بالأسماء + فحص مبكر: العينة غير الصالحة تُعاد ولا تُعد طبيعية + نتيجة غير طبيعية تفتح إحالة لا تُغلق إلا بنتيجة + تحقق مزدوج للتوائم",
        subtitleEn: "Newborns linked to mothers by identifiers, never names + invalid screening samples are recollected, never normal + abnormal results open referrals closed only with an outcome + twin double verification",
        endpoint: "/api/neonatal/newborns?motherPatientId=00000000-0000-0000-0000-000000000000",
        envelopeKey: "newborns",
        columns: [
          { key: "birthWeightG", ar: "وزن الولادة (غ)", en: "Birth weight (g)" },
          { key: "apgar5min", ar: "أبغار 5د", en: "APGAR 5m" },
          { key: "bornAt", ar: "الولادة", en: "Born" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
