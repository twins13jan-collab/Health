import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "tempWorkforce",
        subtitleAr: "تحقق من هوية ومؤهلات مقدمي الخدمة وترخيص سارٍ في تاريخ الوردية ونطاق ممارسة مطابق قبل أي إسناد + منع ازدواج الإسناد بالورديات المتداخلة + حد ساعات العمل 60 ساعة في 7 أيام متداولة يُرفض تجاوزه مهما كانت فجوة التوظيف عاجلة + ورديات وحضور وتقييم موثق",
        subtitleEn: "Identity, qualifications, a licence valid on the shift date and a matching scope of practice are verified before any assignment + double-assignment prevented via overlapping shifts + the 60-hour rolling 7-day working-time limit is refused however urgent the staffing gap + shifts, attendance and evaluations on record",
        endpoint: "/api/temp-workforce/providers",
        envelopeKey: "providers",
        columns: [
          { key: "providerRef", ar: "المقدم", en: "Provider" },
          { key: "profession", ar: "المهنة", en: "Profession" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
