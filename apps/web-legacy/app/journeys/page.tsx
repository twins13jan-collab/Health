import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "journeys",
        subtitleAr: "رحلة واحدة تربط السجلات الحقيقية التي تنشئها وحدات التشغيل لمريض واحد — لقاء وطلبات معمل ووصفات وفواتير وملخص خروج — تحت معرف ارتباط واحد وبترتيب قانوني لا رجوع فيه، وكل خطوة تكتب حدثًا في صندوق الأحداث الصادر: الخيط الذي يجعل مسار المريض عبر المنظومة قابلًا للتتبع بدل جداول منفصلة لا تعرف بعضها",
        subtitleEn: "One journey threads the REAL records that operating modules create for a patient — encounter, lab orders, prescriptions, invoices, discharge summary — under a single correlation id in canonical order with no backwards steps, and every step appends a domain event to the outbox: the thread that makes a patient's path through the system traceable instead of disconnected tables that know nothing of each other",
        endpoint: "/api/journeys",
        envelopeKey: "journeys",
        columns: [
          { key: "correlationId", ar: "معرف الارتباط", en: "Correlation id" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
        createForm: {
          endpoint: "/api/journeys",
          submitAr: "بدء رحلة", submitEn: "Start journey",
          fields: [
            { key: "patientId", ar: "معرف المريض", en: "Patient id" },
            { key: "encounterId", ar: "معرف اللقاء", en: "Encounter id" },
          ],
        },
        rowActions: [
          { labelAr: "إكمال الرحلة", labelEn: "Complete", endpoint: "/api/journeys/{id}/complete" },
        ],
      }}
    />
  );
}
