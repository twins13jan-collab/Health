import ModuleScreen from "@/components/ModuleScreen";

export default function Page() {
  return (
    <ModuleScreen
      config={{
        titleKey: "complexCare",
        subtitleAr: "خطط رعاية متعددة الجهات بمنسق مسمى ومسؤوليات فرق محددة + انتقالات بين المستشفى والمنزل والتأهيل بتسليم موثق وأجهزة منزلية مجهزة قبل التحرك + خطة طوارئ فردية لكل مريض + الإغلاق يقيس استمرارية الرعاية وجودة الحياة لا يحدث بصمت",
        subtitleEn: "Multi-party care plans with a named coordinator and defined team responsibilities + transitions between hospital, home and rehabilitation on a documented handover with home equipment arranged before the move + an individual emergency plan per patient + closure measures continuity and quality of life, never silently",
        endpoint: "/api/complex-care/plans",
        envelopeKey: "plans",
        columns: [
          { key: "patientRef", ar: "المريض", en: "Patient" },
          { key: "coordinator", ar: "المنسق", en: "Coordinator" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
