import ModuleScreen from "@/components/ModuleScreen";

export default function Page() {
  return (
    <ModuleScreen
      config={{
        titleKey: "wounds",
        subtitleAr: "الالتئام يُقاس بمساحة متسلسلة مقابل خط أساس لا بانطباع + اشتباه عدوى يصعّد وحده + صور سريرية بموافقة وعرض مقيد + مواد بتتبع تشغيلة وإنعاش حروق بباركلاند",
        subtitleEn: "Healing is judged by serial area measurements against a baseline, never impression + suspected infection escalates on its own + clinical images need consent and restricted viewing + lot-traced materials and Parkland burn resuscitation",
        endpoint: "/api/wounds/cases?patientId=00000000-0000-0000-0000-000000000000",
        envelopeKey: "cases",
        columns: [
          { key: "classification", ar: "التصنيف", en: "Classification" },
          { key: "site", ar: "الموقع", en: "Site" },
          { key: "tbsaPercent", ar: "TBSA %", en: "TBSA %" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
