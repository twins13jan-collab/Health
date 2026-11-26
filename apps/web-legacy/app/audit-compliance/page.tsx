import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "auditCompliance",
        subtitleAr: "خطط تدقيق وأدلة وملاحظات لا تُغلق إلا بدليل وإجراء تصحيحي وإغلاق موثق + تعارض مصالح وإفصاحات قبل التصرف في قرارات ذات صلة + إشارات احتيال دون أحكام آلية: الإشارة تبقى إشارة حتى يسجل إنسان نتيجتها + قضايا امتثال وإجراءات تصحيحية وحماية سجلات تدقيق بفصل عن التشغيل وطلبات جهات رقابية وإفصاح حوادث أمنية وخصوصية",
        subtitleEn: "Audit plans, evidence and findings that close only with evidence, a corrective action and verified closure + conflicts of interest disclosed before acting on related decisions + fraud signals without automatic judgments: a signal stays a signal until a human records its outcome + compliance cases and corrective actions, audit records protected and separated from operations, regulator requests, and security and privacy incident disclosure",
        endpoint: "/api/audit-compliance/findings",
        envelopeKey: "findings",
        columns: [
          { key: "auditRef", ar: "التدقيق", en: "Audit" },
          { key: "severity", ar: "الخطورة", en: "Severity" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
