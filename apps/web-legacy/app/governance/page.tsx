import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "governance",
        subtitleAr: "موافقات بنطاق دقيق ووصول استثنائي مراجَع وطلبات بحث بلا معرّفات مباشرة (دورة 218)",
        subtitleEn: "Scoped consent, reviewed break-glass access, identifier-free research requests (cycle 218)",
        endpoint: "/api/consents/break-glass/pending-review",
        envelopeKey: "pending_review",
        columns: [
          { key: "reason", ar: "سبب الوصول الاستثنائي", en: "Break-glass reason" },
          { key: "durationMinutes", ar: "المدة (د)", en: "Minutes" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
