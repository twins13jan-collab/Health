import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "complaints",
        subtitleAr: "الاستقبال يسجل الشكوى حرفيًا ولا يغير مضمونها أبدًا + الخطر الوشيك يصعد للسلامة فورًا + المحقق بحد أدنى من الاطلاع وكل اطلاع مسجل + التحقيق منفصل عن القرار + الإغلاق بسبب جذري وإجراء تصحيحي + التظلم لجهة مستقلة",
        subtitleEn: "Reception registers the complaint verbatim and never alters its content + imminent risk escalates to safety at once + the investigator works on the minimum necessary with every access logged + investigation is separated from decision + closure needs a root cause and a corrective action + appeals go to an independent body",
        endpoint: "/api/patient-complaints",
        envelopeKey: "complaints",
        columns: [
          { key: "category", ar: "التصنيف", en: "Category" },
          { key: "content", ar: "المضمون", en: "Content" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
