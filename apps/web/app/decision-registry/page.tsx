import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "decisionRegistry",
        subtitleAr: "تسجيل القرار بصاحب اختصاصه وسنده وسياقه وبدائله المدروسة وأسباب اختياره مربوطًا بموافقاته وبروتوكوله — قرار بلا بدائل موثقة أمرٌ لا قرار + مراجعة وتعديل وإلغاء بسجلات جديدة وأسباب إلزامية: التاريخ لا يُحرَّر أبدًا + قرارات لجان بموقف تعارض مصالح واضح",
        subtitleEn: "Every decision recorded with its competent maker, authority, context, considered alternatives and reasons, linked to its approvals and protocol — a decision without documented alternatives is an order, not a decision + review, amendment and revocation as new records with mandatory reasons: history is never edited + committee decisions with a clear conflicts position",
        endpoint: "/api/decision-registry/decisions",
        envelopeKey: "decisions",
        columns: [
          { key: "title", ar: "القرار", en: "Decision" },
          { key: "decisionMaker", ar: "صاحب القرار", en: "Maker" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
        demoRows: [{ "id": "dr1", "title": "اعتماد بروتوكول قسطرة جديد", "decisionMaker": "المجلس الطبي", "status": "active" }],
      }}
    />
  );
}
