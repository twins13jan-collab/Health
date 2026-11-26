import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "unknownPatient",
        subtitleAr: "هوية مؤقتة آمنة يبدأ بها العلاج دون انتظار وثائق + الدمج في السجل الموثق بأدلة تحقق ومتحقق مستقل عن المنشئ + بيانات الحماية الاجتماعية مقيدة الأدوار + سلسلة تحقق وتعديل محفوظة + الوفاة دون وثائق بإجراء قانوني وجهة مخطرة",
        subtitleEn: "A safe temporary identity that starts care without waiting for documents + merging into the verified record on documented evidence by an independent verifier + social-protection data restricted by role + a preserved verification chain + deaths without documents under legal procedure with the authority notified",
        endpoint: "/api/unknown-patients/",
        envelopeKey: "identities",
        columns: [
          { key: "tempCode", ar: "الكود المؤقت", en: "Temp Code" },
          { key: "circumstance", ar: "الظرف", en: "Circumstance" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
