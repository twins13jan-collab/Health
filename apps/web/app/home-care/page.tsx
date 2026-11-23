import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "homeCare",
        subtitleAr: "كل زيارة مرتبطة بخطة معتمدة طبيًا وفريق ووقتين حقيقيين + تدهور منزلي يصعّد فورًا + أجهزة مسلسلة بحالة تسليم وصيانة + قراءات عن بعد بنفس قانون حداثة العناية المركزة والعنوان الدقيق لا يُخزن أبدًا",
        subtitleEn: "Every visit binds to a physician-approved plan, a team and real times + deterioration at home escalates immediately + serial-tracked devices with delivery condition and maintenance + remote readings under the ICU freshness law, and the exact home address is never stored",
        endpoint: "/api/home-care/referrals?patientId=00000000-0000-0000-0000-000000000000",
        envelopeKey: "referrals",
        columns: [
          { key: "reason", ar: "سبب الإحالة", en: "Reason" },
          { key: "zone", ar: "المنطقة", en: "Zone" },
          { key: "createdAt", ar: "التاريخ", en: "Date" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
        demoRows: [{ "id": "h1", "reason": "تمريض منزلي بعد خروج", "zone": "مدينة نصر", "createdAt": "2026-11-22T13:30:00+02:00", "status": "planned" }],
      }}
    />
  );
}
