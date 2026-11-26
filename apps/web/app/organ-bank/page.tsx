import ModuleScreen from "@/components/ModuleScreen";

export default function Page() {
  return (
    <ModuleScreen
      config={{
        titleKey: "organBank",
        subtitleAr: "تبرع وتقييم وموافقات وتخصيص وفق القانون + لا تخصيص دون موافقة موثقة وتقييم مكتمل وفحص إكراه نظيف واعتماد لجنة + أي حافز غير أخلاقي يوقف الحالة فورًا + سلسلة حيازة وتبريد بوقت إقفار محسوب لكل عضو + تتبع كامل من المصدر إلى المستفيد ومتابعة ما بعد الزراعة",
        subtitleEn: "Lawful donation, evaluation, consent and allocation + no allocation without documented consent, completed evaluation, a clean coercion screen and committee approval + any unethical incentive stops the case outright + custody and cold chain with ischemia timed per graft + full source-to-recipient traceability and post-transplant follow-up",
        endpoint: "/api/organ-bank/donations",
        envelopeKey: "donations",
        columns: [
          { key: "donorCode", ar: "كود المتبرع", en: "Donor Code" },
          { key: "organOrTissue", ar: "العضو/النسيج", en: "Organ/Tissue" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
