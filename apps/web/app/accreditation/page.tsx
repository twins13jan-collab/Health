import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "accreditation",
        subtitleAr: "ملفات ترخيص منشأة وقسم وخدمة وممارسة بمعايير وأدلة وتواريخ انتهاء — منتهي الترخيص خدمة غير مرخصة مهما قالت الشهادة المؤطرة + تفتيش بملاحظات لا يُغلق قبل إتمام التصحيح + تعليق أو تقييد بقرار جهة مختصة مسماة بسندها فقط",
        subtitleEn: "Licence files for facility, department, service and practice with standards, evidence and expiry dates — an expired licence means an unlicensed service whatever the framed certificate says + inspections with findings close only when corrections are complete + suspension or restriction only by a named competent authority's decision",
        endpoint: "/api/accreditation/licences",
        envelopeKey: "licences",
        columns: [
          { key: "holderRef", ar: "الحائز", en: "Holder" },
          { key: "scope", ar: "النطاق", en: "Scope" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
        demoRows: [{ "id": "ac1", "holderRef": "قسم القسطرة", "scope": "service", "status": "active" }],
      }}
    />
  );
}
