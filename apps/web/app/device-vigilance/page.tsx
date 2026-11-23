import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "deviceVigilance",
        subtitleAr: "بلاغات أعطال وأضرار أجهزة بتقييم خطورة وإبلاغ تنظيمي واجب للحالات الخطيرة لا يُغلق الملف قبل إرساله + تصحيح ميداني وحجر واستدعاء يخرج الجهاز من الخدمة في سجل التتبع + كل حادث مرتبط بصيانته ودفعته واحتياج التدريب الذي كشفه",
        subtitleEn: "Device malfunction and harm reports with risk assessment and a regulatory-reporting duty for serious incidents — the case cannot close before it is sent + field correction, quarantine and recall take the device out of service in the tracking record + every incident linked to its maintenance, lot and the training need it revealed",
        endpoint: "/api/device-vigilance/reports",
        envelopeKey: "reports",
        columns: [
          { key: "deviceRef", ar: "الجهاز", en: "Device" },
          { key: "severity", ar: "الخطورة", en: "Severity" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
        demoRows: [{ "id": "dv1", "deviceRef": "مضخة محاليل", "severity": "serious", "status": "assessed" }],
      }}
    />
  );
}
