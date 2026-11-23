import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "programs",
        subtitleAr: "جلسات غسيل بلا تعارض ماكينات + جرعات أورام محسوبة بـBSA وتعديل كلى وتحقق صيدلي",
        subtitleEn: "Dialysis without machine clashes + BSA-based chemo dosing with renal adjustment and pharmacist verification",
        endpoint: "/api/dialysis/sessions?patientId=00000000-0000-0000-0000-000000000000",
        envelopeKey: "sessions",
        columns: [
          { key: "machineCode", ar: "الماكينة", en: "Machine" },
          { key: "startsAt", ar: "البدء", en: "Starts" },
          { key: "endsAt", ar: "الانتهاء", en: "Ends" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
        demoRows: [{"id": "d1", "machineCode": "HD-03", "startsAt": "2026-11-23T08:00:00+02:00", "endsAt": "2026-11-23T12:00:00+02:00", "status": "scheduled"}],
      }}
    />
  );
}
