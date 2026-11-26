import ModuleScreen from "@/components/ModuleScreen";

export default function Page() {
  return (
    <ModuleScreen
      config={{
        titleKey: "mobileDevices",
        subtitleAr: "تسجيل أجهزة وربطها بمستخدم ومنشأة كهوية جهازية + جلسات وتشفير وتحديثات وإبطال: غير المشفر لا يحمل جلسة ودون الحد المدعوم يُحدّث أولًا + مسح آمن عند فقد أو انتهاء تفويض بسبب مشروع فقط + إشعارات بتأكيد استلام وضبط كاميرا وباركود وموقع وملفات",
        subtitleEn: "Devices registered and bound to a user and facility as a device identity + sessions, encryption, updates and revocation: an unencrypted device holds no session and a below-minimum app updates first + secure wipe on loss or end of authorization for a legitimate reason only + notifications with delivery acknowledgment and camera, barcode, location and file controls",
        endpoint: "/api/mobile-devices/devices",
        envelopeKey: "devices",
        columns: [
          { key: "deviceRef", ar: "الجهاز", en: "Device" },
          { key: "userRef", ar: "المستخدم", en: "User" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
