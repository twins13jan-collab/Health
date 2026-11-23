import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "longitudinal",
        subtitleAr: "هوية موحدة ومعرف مؤقت لا يعطل الرعاية أبدًا + مطابقة مفسرة ودمج عالي المخاطر باعتماد مزدوج وفصل مهام + قيم متعارضة تبقى ظاهرة بمصدرها ولا تستبدل بصمت + فك دمج قابل للتدقيق",
        subtitleEn: "A unified identity with a temporary identity that never blocks care + explainable matching with dual approval and separation of duties for high-risk merges + conflicting values stay visible with their source, never silently overwritten + an auditable unmerge",
        endpoint: "/api/longitudinal/identities/00000000-0000-0000-0000-000000000000/timeline",
        envelopeKey: "records",
        columns: [
          { key: "recordType", ar: "نوع السجل", en: "Record type" },
          { key: "recordRef", ar: "المرجع", en: "Reference" },
          { key: "sourceSystem", ar: "المصدر", en: "Source" },
          { key: "ownerFacility", ar: "المالك", en: "Owner" },
        ],
        demoRows: [{ "id": "lr1", "recordType": "diagnosis", "recordRef": "ENC-2210", "sourceSystem": "العيادات", "ownerFacility": "المستشفى المركزي" }],
      }}
    />
  );
}
