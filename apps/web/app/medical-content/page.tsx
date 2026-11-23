import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "medicalContent",
        subtitleAr: "مكتبة تثقيف معتمدة ومراجعة طبيًا: أمراض وأدوية وتحضير ورعاية منزلية بلغة المريض وبصيغ متاحة + لا نشر دون اعتماد سريري مستقل عن المؤلف ومصدر ومراجع موثقة + المراجعة تنتهي بعد 12 شهرًا والمحتوى المنتهي يُراجع أو يُسحب + حملات تقيس الفهم لا الوصول فقط",
        subtitleEn: "An approved, medically reviewed education library: diseases, medications, preparation and home care in the patient's language with accessible formats + no publication without clinical approval independent of the author, with source and references documented + reviews expire after 12 months and stale content is re-reviewed or withdrawn + campaigns measure comprehension, not just reach",
        endpoint: "/api/medical-content/",
        envelopeKey: "items",
        columns: [
          { key: "title", ar: "المحتوى", en: "Content" },
          { key: "topic", ar: "الموضوع", en: "Topic" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
        demoRows: [{ "id": "mc1", "title": "العناية بقدم السكري في المنزل", "topic": "home_care", "status": "published" }],
      }}
    />
  );
}
