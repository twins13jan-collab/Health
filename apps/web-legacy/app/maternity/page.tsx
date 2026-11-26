import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "maternity",
        subtitleAr: "حمل وولادة ونفاس: عمر حملي وموعد متوقع محسوبان + خطورة استشارية لا تُطبق دون اعتماد الطبيب + نزف ما بعد الولادة بمسار طوارئ + سجل ولادة معتمد لا يُعدَّل",
        subtitleEn: "Pregnancy, delivery & postpartum: computed GA/EDD + advisory risk never applied without physician confirmation + PPH emergency path + immutable signed-off delivery records",
        endpoint: "/api/maternity/pregnancies?patientId=00000000-0000-0000-0000-000000000000",
        envelopeKey: "pregnancies",
        columns: [
          { key: "edd", ar: "الموعد المتوقع", en: "EDD" },
          { key: "fetusCount", ar: "الأجنة", en: "Fetuses" },
          { key: "riskLevel", ar: "الخطورة", en: "Risk", status: true },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
