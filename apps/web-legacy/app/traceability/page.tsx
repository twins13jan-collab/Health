import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "traceability",
        subtitleAr: "تحديد الوحدات المنشئة والمستهلكة لكل كيان بيانات ومالكه الوحيد — مالكان يعني بلا مالك والتعارض يُحسم لا يُخزن + انتقال مسؤولية بين مستويات الرعاية بأدوار مصرح لها مسماة + ربط السجل السريري بالمخزون والمالية والموارد وكشف التكرار والتعارض وصلاحيات كل انتقال",
        subtitleEn: "Which modules create and consume each data entity and its single owner — two owners means no owner, and a conflict is resolved, not stored + responsibility transfers between levels of care with named authorized roles + the clinical record linked to inventory, finance and resources, with duplication and conflicts detected and every transfer's permissions defined",
        endpoint: "/api/traceability/flows",
        envelopeKey: "flows",
        columns: [
          { key: "dataEntity", ar: "كيان البيانات", en: "Data entity" },
          { key: "producerModule", ar: "المنشئة", en: "Producer" },
          { key: "ownerModule", ar: "المالك", en: "Owner" },
        ],
      }}
    />
  );
}
