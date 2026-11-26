import ModuleScreen from "@/components/ModuleScreen";

export default function Page() {
  return (
    <ModuleScreen
      config={{
        titleKey: "citizenPortal",
        subtitleAr: "لا إتاحة قبل التحقق من الهوية + الممثل يعمل داخل نطاق تمثيل معتمد سارٍ فقط + كل إفصاح مسجل بقناته + الرسائل لا تسافر إلا على قنوات فعّلها المواطن والتنبيهات السريرية بموافقتها الخاصة + صيغ ميسرة لذوي الإعاقة جزء من الحساب",
        subtitleEn: "No disclosure before identity verification + representatives act only inside an approved, unexpired scope + every disclosure is logged with its channel + messages travel only on citizen-enabled channels and clinical alerts need their own consent + accessible formats are part of the account",
        endpoint: "/api/citizen-portal/accounts",
        envelopeKey: "accounts",
        columns: [
          { key: "fullName", ar: "المواطن", en: "Citizen" },
          { key: "citizenRef", ar: "المرجع", en: "Reference" },
          { key: "identityVerified", ar: "هوية موثقة", en: "Verified" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
