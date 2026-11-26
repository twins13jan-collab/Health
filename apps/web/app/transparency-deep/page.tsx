import ModuleScreen from "@/components/ModuleScreen";

export default function Page() {
  return (
    <ModuleScreen
      config={{
        titleKey: "transparencyDeep",
        subtitleAr: "بيانات قابلة للنشر وفق القانون بمصدر وتاريخ تحديث ومنهج ظاهرة وبلا بيانات حساسة أبدًا + طلبات معلومات وردود + تصحيح معلومات منشورة وسحب غير الدقيق بملاحظة ظاهرة: سجل عام يتغير بلا أثر يعلّم الجمهور ألا يثق بشيء ينشره + واجهات بيانات عامة مجمعة",
        subtitleEn: "Publishable data under the law, with source, update date and methodology shown and never any sensitive data + information requests and responses + published information corrected and the inaccurate withdrawn with a visible note: a public record that changes without a trace teaches the public to trust nothing it publishes + aggregated public data interfaces",
        endpoint: "/api/transparency-deep/publications",
        envelopeKey: "publications",
        columns: [
          { key: "title", ar: "العنوان", en: "Title" },
          { key: "sourceRef", ar: "المصدر", en: "Source" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
