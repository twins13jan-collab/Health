import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "bloodBank",
        subtitleAr: "وحدات دم بحجز مشروط بتوافق ABO/Rh ورفض المنتهي (دورة 223)",
        subtitleEn: "Blood units reserved only after ABO/Rh compatibility and expiry checks (cycle 223)",
        endpoint: "/api/blood-bank/units",
        envelopeKey: "units",
        columns: [
          { key: "unitCode", ar: "رمز الوحدة", en: "Unit code" },
          { key: "bloodGroup", ar: "الفصيلة", en: "Group" },
          { key: "component", ar: "المكوّن", en: "Component" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
        demoRows: [{"id": "d1", "unitCode": "BU-1001", "bloodGroup": "O-", "component": "RBC", "status": "available"}, {"id": "d2", "unitCode": "BU-1002", "bloodGroup": "A+", "component": "PLASMA", "status": "reserved"}],
      }}
    />
  );
}
