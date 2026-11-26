import ModuleScreen from "@/components/ModuleScreen";

export default function Page() {
  return (
    <ModuleScreen
      config={{
        titleKey: "radiologyDeep",
        subtitleAr: "طلب فحص بمبرر وأولوية وموانع مفحوصة + تحضير وتباين ومخاطر كلى: الصبغة قرار كلى قبل قرار صورة، ودون eGFR 30 تحتاج تجاوز كلى موثقًا + قوائم عمل أجهزة وتقارير أولية ونهائية وتصحيح يحل محلًا لا تحريرًا + نتائج حرجة باستلام موثق من مستقبل مسمى وربط DICOM/PACS وجرعات إشعاع وفحوص متكررة",
        subtitleEn: "An exam order with justification, priority and checked contraindications + preparation, contrast and kidney risk: contrast is a kidney decision before an image decision, and below eGFR 30 it needs a documented nephrology override + modality worklists, preliminary and final reports, corrections that supersede rather than edit + critical results with documented receipt by a named receiver, DICOM/PACS linkage, radiation doses and repeat exams",
        endpoint: "/api/radiology-deep/orders",
        envelopeKey: "orders",
        columns: [
          { key: "patientRef", ar: "المريض", en: "Patient" },
          { key: "exam", ar: "الفحص", en: "Exam" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
