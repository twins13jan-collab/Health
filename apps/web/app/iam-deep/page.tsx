import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "iamDeep",
        subtitleAr: "دورة حياة حسابات بشرية وآلية وخدمات وواجهات + SSO وMFA وسياسات كلمات مرور ومفاتيح مرور وأجهزة موثوقة + صلاحيات حسب دور وصفة ومنشأة ومحافظة ومورد وغرض وعملية — والمنح لغرض مسمى وباعتماد غير صاحبه: منح الذات غياب ضبط لا ضبط، والتفويض المؤقت يموت بانتهائه حسابيًا لا بذاكرة أحد + مراجعات استحقاق وفصل واجبات ودورة انضمام ونقل وإنهاء بإبطال جلسات",
        subtitleEn: "The lifecycle of human, machine, service and interface accounts + SSO, MFA, password policies, passkeys and trusted devices + permissions by role, attribute, facility, governorate, vendor, purpose and operation — a grant names its purpose and is approved by someone other than its holder: self-grant is the absence of access control, and a temporary grant dies at its expiry by computation, not by anyone's memory + entitlement reviews, separation of duties, and joiner, mover and leaver handling with session revocation",
        endpoint: "/api/iam-deep/grants",
        envelopeKey: "grants",
        columns: [
          { key: "subjectRef", ar: "صاحب الصلاحية", en: "Subject" },
          { key: "roleName", ar: "الدور", en: "Role" },
          { key: "purpose", ar: "الغرض", en: "Purpose" },
        ],
        demoRows: [{ "id": "ia1", "subjectRef": "فني دعم", "roleName": "قراءة سجلات", "purpose": "تذكرة دعم 441" }],
      }}
    />
  );
}
