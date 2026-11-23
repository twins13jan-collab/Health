import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "moduleSpecs",
        subtitleAr: "توصيف كل وحدة وظيفية باسم وغرض ونطاق ومالك ومستخدمين وأدوار + دورة عمل بحالات وانتقالات وموافقات واستثناءات وتصعيد + كيانات وحقول حساسة ومصدر حقيقة واحتفاظ + واجهات وأحداث ورسائل ومفاتيح عدم تكرار + صلاحيات وعزل وتدقيق وتفويض — والاعتماد يشهد على توصيف كامل فقط",
        subtitleEn: "Every functional module specified by name, purpose, scope, owner, users and roles + a workflow with states, transitions, approvals, exceptions and escalation + entities, sensitive fields, source of truth and retention + interfaces, events, messages and idempotency keys + permissions, isolation, audit and delegation — accreditation certifies a complete specification only",
        endpoint: "/api/module-specs/specifications",
        envelopeKey: "specifications",
        columns: [
          { key: "moduleRef", ar: "الوحدة", en: "Module" },
          { key: "ownerOrg", ar: "المالك", en: "Owner" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
        demoRows: [{ "id": "ms1", "moduleRef": "وحدة المختبر", "ownerOrg": "إدارة المعامل", "status": "draft" }],
      }}
    />
  );
}
