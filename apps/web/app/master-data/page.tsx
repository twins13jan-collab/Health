import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "masterData",
        subtitleAr: "كل كتالوج له مالك ومصدر وإصدار + رمز غير معروف يذهب للمراجعة ولا يحول تخمينًا أبدًا + رمز موقوف يبقى مقروءًا تاريخيًا ولا يدخل معاملة جديدة إلا باستثناء معتمد من الحوكمة",
        subtitleEn: "Every catalogue has an owner, a source and a version + an unknown code goes to review and is never mapped by guessing + a deprecated code stays readable in history but enters new transactions only with a governance-approved exception",
        endpoint: "/api/master-data/codes/review-queue",
        envelopeKey: "codes",
        columns: [
          { key: "codeSystem", ar: "النظام", en: "System" },
          { key: "code", ar: "الرمز", en: "Code" },
          { key: "description", ar: "الوصف", en: "Description" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
        demoRows: [{ "id": "md1", "codeSystem": "LOCAL", "code": "DX-991", "description": "تشخيص محلي غير معروف", "status": "unknown_review" }],
      }}
    />
  );
}
