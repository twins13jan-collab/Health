import ModuleScreen from "@/components/ModuleScreen";

export default function Page() {
  return (
    <ModuleScreen
      config={{
        titleKey: "refDataQuality",
        subtitleAr: "معرفات موحدة لمنشآت وأقسام وخدمات وممارسين + مطابقة أكواد محلية ومرجعية وخرائط تحويل بين أنظمة موردين + رصد قيم غير معروفة أو غير متوافقة دون إسقاط صامت + مراجعة بشرية لتطابقات حساسة قبل تفعيلها: مطابقة آلية واسعة تصنع حقيقة رسمية خاطئة في آلاف السجلات + حفظ تاريخ كل تغيير في الخرائط",
        subtitleEn: "Unified identifiers for facilities, departments, services and practitioners + local-to-reference code matching and conversion maps between vendor systems + unknown or incompatible values monitored, never silently dropped + human review of sensitive matches before activation: automated matching at scale manufactures official falsehood in thousands of records at once + the history of every map change preserved",
        endpoint: "/api/ref-data-quality/mappings",
        envelopeKey: "mappings",
        columns: [
          { key: "localCode", ar: "الرمز المحلي", en: "Local code" },
          { key: "referenceCode", ar: "الرمز المرجعي", en: "Reference code" },
          { key: "active", ar: "مفعّل", en: "Active" },
        ],
      }}
    />
  );
}
