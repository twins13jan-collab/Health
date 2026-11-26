import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "icu",
        subtitleAr: "إقامات عناية مركزة: حداثة بيانات الأجهزة لا تُعرض كحية إن تقادمت + حساب جرعات بالوزن ممنوع قبل التحقق من الوزن المرجعي + تصعيد موثق بأزمنة استجابة",
        subtitleEn: "ICU stays: stale device data is never shown as live + weight-based dosing blocked until the reference weight is verified + documented escalations with response times",
        endpoint: "/api/icu/stays?patientId=00000000-0000-0000-0000-000000000000",
        envelopeKey: "stays",
        columns: [
          { key: "admissionReason", ar: "سبب القبول", en: "Admission reason" },
          { key: "careLevel", ar: "مستوى الرعاية", en: "Care level" },
          { key: "admittedAt", ar: "القبول", en: "Admitted" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
