import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "facilitySafety",
        subtitleAr: "خطط حريق وإخلاء بمسارات ونقاط تجمع مسماة + فحص الإنذار والإطفاء والغازات والمولدات بعيوب وإجراءات تصحيحية + تصاريح الأعمال الخطرة باعتماد مستقل ومراقبة حريق وقفل ووسم + دخول المناطق الحساسة بمرافقة ومدة محددة + تمارين إخلاء بزمن مقاس",
        subtitleEn: "Fire and evacuation plans with named routes and assembly points + alarm, extinguisher, gas and generator inspections with defects and corrective actions + hot-work permits independently approved with fire watch and lockout/tagout + sensitive-area access escorted and time-boxed + evacuation drills with measured times",
        endpoint: "/api/facility-safety/permits",
        envelopeKey: "permits",
        columns: [
          { key: "workType", ar: "العمل", en: "Work" },
          { key: "location", ar: "الموقع", en: "Location" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
        demoRows: [{ "id": "fs1", "workType": "welding", "location": "ورشة الصيانة", "status": "approved" }],
      }}
    />
  );
}
