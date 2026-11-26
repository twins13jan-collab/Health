import ModuleScreen from "@/components/ModuleScreen";

export default function Page() {
  return (
    <ModuleScreen
      config={{
        titleKey: "capacity",
        subtitleAr: "إشغال المنشآت وطنيًا — مجاميع فقط دون تفاصيل مرضى (دورة 225)",
        subtitleEn: "National facility occupancy — aggregates only, never patient details (cycle 225)",
        endpoint: "/api/capacity/board",
        envelopeKey: "board",
        columns: [
          { key: "name_ar", ar: "المنشأة", en: "Facility" },
          { key: "governorate", ar: "المحافظة", en: "Governorate" },
          { key: "bed_capacity", ar: "السعة", en: "Beds" },
          { key: "occupancy_pct", ar: "الإشغال %", en: "Occupancy %" },
        ],
      }}
    />
  );
}
