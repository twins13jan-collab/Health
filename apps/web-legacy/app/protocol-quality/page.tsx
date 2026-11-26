import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "protocolQuality",
        subtitleAr: "ربط كل بروتوكول بمصدره العلمي وجهة اعتماده ونطاقه واستثناءاته وموانعه + تواريخ مراجعة وسحب وتحديث: المسحوب لا يلزم أحدًا والمنتهية مراجعته متقادم حتى يُراجع + قياس التزام وانحرافات مبررة بسجل وغير مبررة — فوق 10% غير مبررة مراجعة جودة + تنبيهات ترتبط بالإصدار الساري فقط",
        subtitleEn: "Every protocol linked to its scientific source, approving body, scope, exceptions and contraindications + review, withdrawal and update dates: a withdrawn protocol binds no one and an out-of-review one is stale until re-reviewed + adherence measured with deviations justified on record or counted unjustified — above 10% unjustified triggers quality review + alerts attach to the live version only",
        endpoint: "/api/protocol-quality/protocols",
        envelopeKey: "protocols",
        columns: [
          { key: "name", ar: "البروتوكول", en: "Protocol" },
          { key: "version", ar: "الإصدار", en: "Version" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
