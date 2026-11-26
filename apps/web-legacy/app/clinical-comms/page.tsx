import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "clinicalComms",
        subtitleAr: "بلاغات حرجة بين الطبيب والتمريض والمعمل والأشعة والصيدلة بحلقة مغلقة: تأكيد استلام وإعادة قراءة قبل أن يُعدّ البلاغ مُبلَّغًا + بلاغ صامت 15 دقيقة يتصاعد ولا يشيخ في صندوق وارد + قنوات بديلة عند تعطل النظام موثقة الاستخدام",
        subtitleEn: "Critical notifications between physician, nursing, laboratory, radiology and pharmacy run closed-loop: acknowledged and read back before the message counts as communicated + a silent alert escalates after 15 minutes instead of aging in an inbox + fallback channels used and recorded when the system channel is down",
        endpoint: "/api/clinical-comms/alerts",
        envelopeKey: "alerts",
        columns: [
          { key: "patientRef", ar: "المريض", en: "Patient" },
          { key: "messageType", ar: "النوع", en: "Type" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
