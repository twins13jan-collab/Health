import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "construction",
        subtitleAr: "دراسة احتياج وخريطة صحية واعتماد تصميم طبي وتصميم مكافحة عدوى قبل أي إنشاء — مكافحة العدوى تراجع الرسومات لا الجدران المنتهية + أوامر تغيير لا تُطبق إلا معتمدة + التسليم بنجاح تشغيل كل الأنظمة الحرجة واستلام الأصول والوثائق والتراخيص: فشل الغازات الطبية مستشفى لا يفتح",
        subtitleEn: "Need study, health map, medical design and infection-control design approvals before any construction — infection control reviews drawings, not finished walls + change orders apply only when approved + handover only with every critical system commissioned and assets, documents and licences received: a failed medical-gas test is a hospital that does not open",
        endpoint: "/api/construction/projects",
        envelopeKey: "projects",
        columns: [
          { key: "name", ar: "المشروع", en: "Project" },
          { key: "location", ar: "الموقع", en: "Location" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
