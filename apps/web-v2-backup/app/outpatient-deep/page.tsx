import ModuleScreen from "@/components/ModuleScreen";

export default function Page() {
  return (
    <ModuleScreen
      config={{
        titleKey: "outpatientDeep",
        subtitleAr: "دورة زيارة من طلب وتحقق هوية وأهلية حتى فرز وكشف وخطة علاجية وإغلاق + جدولة حسب تخصص وطبيب وغرفة وتجهيز وطاقة فعلية + زيارات حضورية وافتراضية ومنزلية وهاتفية + قوائم انتظار وتأخير وإلغاء وعدم حضور وإعادة جدولة — والزيارة لا تغلق بلا مسؤول متابعة مسمى، ومن لم يحضر يُسجل ويُعرض عليه موعد جديد لا يُسقط بصمت",
        subtitleEn: "A visit cycle from request, identity and eligibility verification through triage, examination, the treatment plan and closure + scheduling by specialty, physician, room, equipment and real capacity + in-person, virtual, home and phone visits + waiting lists, delays, cancellations, no-shows and rescheduling — a visit closes only with a named follow-up owner, and a no-show is recorded with a reschedule offered, never silently dropped",
        endpoint: "/api/outpatient-deep/visits",
        envelopeKey: "visits",
        columns: [
          { key: "patientRef", ar: "المريض", en: "Patient" },
          { key: "specialty", ar: "التخصص", en: "Specialty" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
