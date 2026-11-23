import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "admissionsDeep",
        subtitleAr: "طلب تنويم بمبرر وأولوية قبول وتخصص مطلوب + قوائم انتظار أسرة وتحويل بين أقسام ومنشآت بقبول الجهة المستقبلة + تقييم دخول وخطة رعاية يومية ومراجعة ملاءمة مستوى رعاية + تخطيط خروج مبكر وملخص بتشخيصات وإجراءات وتعليمات وتحذيرات وأدوية ومتابعة — وملخص بلا تحذيرات يرسل المريض بيته أعزل، وخروج ضد النصيحة بشرح مخاطر وتوقيع",
        subtitleEn: "An admission request with justification, acceptance priority and required specialty + bed waiting lists and transfers between departments and facilities with the receiving side's acceptance + admission assessment, a daily care plan and level-of-care review + early discharge planning and a summary with diagnoses, procedures, instructions, warnings, medications and follow-up — a summary without warnings sends the patient home unarmed, and discharge against advice needs risks explained and signed",
        endpoint: "/api/admissions-deep/admissions",
        envelopeKey: "admissions",
        columns: [
          { key: "patientRef", ar: "المريض", en: "Patient" },
          { key: "specialty", ar: "التخصص", en: "Specialty" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
        demoRows: [{ "id": "ad1", "patientRef": "P-771", "specialty": "باطنة", "status": "requested" }],
      }}
    />
  );
}
