import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "cardiology",
        subtitleAr: "مسارات ألم الصدر بساعات موثقة من الباب إلى البالون وهدف 90 دقيقة يوسم تجاوزه + لا قسطرة قبل اكتمال تجهيز المريض والفريق والموافقات + الدعامات والأجهزة المزروعة برقم تسلسلي مرتبط بالتكلفة والمطالبة + متابعة النزيف والكلى واضطراب النظم",
        subtitleEn: "Chest-pain pathways on documented clocks from door to balloon with the 90-minute target flagged on breach + no cath before complete patient, team and consent preparation + stents and implanted devices traceable by serial and linked to cost and claim + bleeding, kidney and rhythm follow-up",
        endpoint: "/api/cardiology/pathways",
        envelopeKey: "pathways",
        columns: [
          { key: "patientRef", ar: "المريض", en: "Patient" },
          { key: "ecgFinding", ar: "التخطيط", en: "ECG" },
          { key: "pathway", ar: "المسار", en: "Pathway" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
