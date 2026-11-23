import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "protocolVersions",
        subtitleAr: "بروتوكولات علاجية حسب تخصص وحالة وفئة عمرية ومحددات استخدام + مرجع علمي وجهة مالكة ولجنة معتمدة وتواريخ إصدار وسريان ومراجعة: الإصدار لا يسري إلا مكتملًا + تعديلات وتعليقات وموافقات واستثناءات وسحب إصدار قديم + ربط كل جلسة وإجراء وجرعة وقرار بالإصدار المستخدم وسجل يجيب أي بروتوكول حكم خدمة سابقة",
        subtitleEn: "Treatment protocols by specialty, condition, age group and use determinants + scientific reference, owning body, approving committee and issue, effective and review dates: a version goes live only complete + amendments, comments, approvals, exceptions and withdrawal of the old version + every session, procedure, dose and decision linked to the version used, and a register that answers which protocol governed a past service",
        endpoint: "/api/protocol-versions/versions",
        envelopeKey: "versions",
        columns: [
          { key: "code", ar: "الرمز", en: "Code" },
          { key: "version", ar: "الإصدار", en: "Version" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
        demoRows: [{ "id": "pv1", "code": "ACS-01", "version": "3.2", "status": "active" }],
      }}
    />
  );
}
