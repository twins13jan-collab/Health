import ModuleScreen from "@/components/ModuleScreen";

export default function Page() {
  return (
    <ModuleScreen
      config={{
        titleKey: "securityIncidents",
        subtitleAr: "أدلة ببصمات وسلسلة حيازة لا تُعدل + حساب مخترق لا يُحتوى قبل إبطال جلساته + فدية لا استعادة لها قبل نسخ نظيفة موثقة + نطاق تسريب غير مؤكد لا يُنشر رقمه + قرار إخطار مبرر دائمًا",
        subtitleEn: "Hashed evidence with an unmodifiable custody chain + a compromised account is not contained before its sessions are revoked + no ransomware recovery before a verified clean backup + an unconfirmed leak scope publishes no number + every notification decision is justified",
        endpoint: "/api/security-incidents",
        envelopeKey: "incidents",
        columns: [
          { key: "type", ar: "النوع", en: "Type", status: true },
          { key: "systems", ar: "الأنظمة", en: "Systems" },
          { key: "severity", ar: "الخطورة", en: "Severity", status: true },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
