import ModuleScreen from "@/components/ModuleScreen";

export default function Page() {
  return (
    <ModuleScreen
      config={{
        titleKey: "oncology",
        subtitleAr: "كل دورة مرتبطة بإصدار بروتوكول معتمد طبيًا وسارٍ — ووجود البروتوكول بالنظام لا يعني اعتماده + النظام لا يولد جرعة أبدًا بل يسجل ما اعتمده الاستشاري + تحقق مستقل قبل التحضير والإعطاء ويعلق عند خروج الوزن أو التحاليل عن الشروط + دواء مستدعى يمنع الصرف والبديل بقرار طبي + السمية تقرر الاستمرار أو التعديل أو الإيقاف",
        subtitleEn: "Every cycle binds to a medically approved, in-date protocol version — a protocol merely existing is not an approval + the system never generates a dose, it records what the consultant approved + independent verification before preparation and administration, suspended when weight or labs leave protocol conditions + a recalled drug blocks dispensing and a substitute needs a medical decision + toxicity drives continue, modify or stop",
        endpoint: "/api/oncology/cases",
        envelopeKey: "cases",
        columns: [
          { key: "tumorType", ar: "الورم", en: "Tumor" },
          { key: "stage", ar: "المرحلة", en: "Stage" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
