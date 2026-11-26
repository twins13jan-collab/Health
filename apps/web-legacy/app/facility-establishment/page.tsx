import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "facilityEstablishment",
        subtitleAr: "دراسة احتياج سكاني وخريطة صحية تبرر المنشأة الجديدة + موقع وتصميم باشتراطات طبية ومكافحة عدوى + مقاولو وعقود ومستخلصات وتغييرات ومخاطر وجودة تنفيذ + توريد وتركيب واختبار أجهزة وأثاث وبنية وأنظمة + استلام وتشغيل تجريبي وتراخيص واعتمادات وتدريب — والافتتاح عند اكتمال كل بوابة: الشريط ليس بوابة",
        subtitleEn: "A population-need study and health map justify the new facility + site and design under medical requirements and infection control + contractors, contracts, extracts, changes, risks and execution quality + equipment, furniture, infrastructure and systems supplied, installed and tested + handover, trial operation, licences, accreditations and training — opening when every gate has passed: a ribbon is not a gate",
        endpoint: "/api/facility-establishment/projects",
        envelopeKey: "projects",
        columns: [
          { key: "name", ar: "المشروع", en: "Project" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
