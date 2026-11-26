import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "serviceDesk",
        subtitleAr: "بلاغات مرتبطة بالخدمة والمنشأة والجهاز: أجهزة واتصال وطباعة وباركود وبرمجيات وحسابات + تصنيف وتصعيد وحل ضد اتفاقيات زمن استجابة لكل خطورة + الحوادث الأمنية والسريرية في مسارات خاصة لا تُحل إلا بسلطة مسارها + قياس الرضا وقاعدة معرفة للحلول المثبتة",
        subtitleEn: "Reports tied to service, facility and device: hardware, connectivity, printing, barcode, software and accounts + classification, escalation and resolution against per-severity response SLAs + security and clinical incidents in separate tracks resolved only by their own authority + satisfaction measured and proven fixes feeding a knowledge base",
        endpoint: "/api/service-desk/tickets",
        envelopeKey: "tickets",
        columns: [
          { key: "category", ar: "التصنيف", en: "Category" },
          { key: "severity", ar: "الخطورة", en: "Severity" },
          { key: "track", ar: "المسار", en: "Track" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
