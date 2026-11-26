import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "ethicsCommittees",
        subtitleAr: "تشكيل لجان واختصاصاتها ومراجعة حالات تعارض أخلاقي وتقنيات وذكاء اصطناعي وأبحاث وبروتوكولات عالية المخاطر + قرار لا يصح بلا نصاب + تصويت فقط لمن أعلن تضارب مصالحه — صوت بمصلحة غير معلنة يلوث القرار + تنحٍّ موثق لا مهموس",
        subtitleEn: "Committee formation and mandates with review of ethically conflicted cases, technologies, AI, research and high-risk protocols + no valid decision without quorum + only members with declared conflicts of interest may vote — a vote with an undeclared interest contaminates the decision + recusals recorded, not whispered",
        endpoint: "/api/ethics-committees/committees",
        envelopeKey: "committees",
        columns: [
          { key: "name", ar: "اللجنة", en: "Committee" },
          { key: "mandate", ar: "الاختصاص", en: "Mandate" },
          { key: "quorumRequired", ar: "النصاب", en: "Quorum" },
        ],
      }}
    />
  );
}
