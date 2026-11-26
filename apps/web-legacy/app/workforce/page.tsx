import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "workforce",
        subtitleAr: "ورديات بلا تداخل وراحة دنيا 11 ساعة (دورة 231)",
        subtitleEn: "Rosters with no overlaps and a minimum 11-hour rest rule (cycle 231)",
        endpoint: "/api/workforce/staff",
        envelopeKey: "staff",
        columns: [
          { key: "fullName", ar: "الاسم", en: "Name" },
          { key: "profession", ar: "المهنة", en: "Profession" },
          { key: "specialty", ar: "التخصص", en: "Specialty" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
