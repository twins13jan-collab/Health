import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "legalCompliance",
        subtitleAr: "سجل قوانين ولوائح ومعايير مربوط كل متطلب فيها بسياسته وإجرائه وضابطه ودليله + نفاذ وتعديل وإلغاء بتواريخ — المتطلب يلزم داخل نافذته فقط + استثناءات بسند صلاحية وأجل انتهاء: استثناء بلا أجل إلغاء لم يصوت عليه أحد + مراجعات امتثال لا تُغلق عند عدم الامتثال قبل التصحيح",
        subtitleEn: "A register of laws, regulations and standards with every requirement linked to its policy, procedure, control and evidence + effective, amendment and repeal dates — a requirement binds only inside its window + exceptions with an authority basis and an expiry: an exception without an expiry is a repeal nobody voted on + compliance reviews that never close on non-compliance before correction",
        endpoint: "/api/legal-compliance/requirements",
        envelopeKey: "requirements",
        columns: [
          { key: "title", ar: "المتطلب", en: "Requirement" },
          { key: "sourceType", ar: "المصدر", en: "Source" },
          { key: "effectiveFrom", ar: "النفاذ من", en: "Effective from" },
        ],
      }}
    />
  );
}
