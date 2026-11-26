import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "deathData",
        subtitleAr: "تسجيل وفاة بتوقيت ومكان ومصدر + أسباب وفاة بترميز معتمد ومصدقة قبل أي ربط إحصائي رسمي + مراجعة وفيات ومؤشرات قابلة للتجنب + تصحيح تعارضات بسجل تاريخي: الحقل والقيمة القديمة والجديدة والسبب، فالسجل لا يُكتب فوقه بصمت أبدًا + حماية بيانات المتوفى والأسرة",
        subtitleEn: "Death registration with time, place and source + causes of death under approved coding, certified before any official statistical linkage + review of deaths and avoidable-mortality indicators + conflict correction with a historical record: the field, the old and new values and the reason, so the record is never silently overwritten + protection of the deceased's and the family's data",
        endpoint: "/api/death-data/registrations",
        envelopeKey: "registrations",
        columns: [
          { key: "deceasedRef", ar: "المرجع", en: "Reference" },
          { key: "causeCode", ar: "سبب الوفاة (مرمز)", en: "Cause (coded)" },
          { key: "statisticsLinked", ar: "رُبط إحصائيًا", en: "Statistics linked" },
        ],
      }}
    />
  );
}
