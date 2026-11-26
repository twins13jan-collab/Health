import ModuleScreen from "@/components/ModuleScreen";

export default function Page() {
  return (
    <ModuleScreen
      config={{
        titleKey: "deathRegistry",
        subtitleAr: "سجل وفيات المنشأة: تاريخ الوفاة وسببها مرمّزًا بـ ICD-10 مع الوصف والمُصدِّق — والتصحيح يتم بمسار موثق لا بتعديل صامت",
        subtitleEn: "The facility death register: date of death and its ICD-10 coded cause with description and certifier — corrections run through a documented path, never a silent edit",
        endpoint: "/api/death-registry",
        envelopeKey: "records",
        columns: [
          { key: "diedAt", ar: "تاريخ الوفاة", en: "Died at" },
          { key: "causeIcd10", ar: "سبب الوفاة (ICD-10)", en: "Cause (ICD-10)" },
          { key: "causeDescription", ar: "وصف السبب", en: "Cause description" },
          { key: "certifiedBy", ar: "المُصدِّق", en: "Certified by" },
        ],
      }}
    />
  );
}
