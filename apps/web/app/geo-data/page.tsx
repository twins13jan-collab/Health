import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "geoData",
        subtitleAr: "حدود إدارية ومواقع منشآت بإحداثيات موثقة ودقة معلنة وتاريخ تحقق — والتحقق الأقدم من عام يظهر قديمًا + نطاقات تغطية وإحالة وخدمات متنقلة + تحليل زمن وصول على شبكة طرق موثوقة + المسافة الخطية لا تُعرض أبدًا باعتبارها زمنًا فعليًا",
        subtitleEn: "Administrative boundaries and facility locations with documented coordinates, stated accuracy and a verification date — verifications older than a year show as stale + coverage, referral and mobile-service zones + access-time analysis on a trusted road network + straight-line distance is never presented as actual travel time",
        endpoint: "/api/geo-data/locations",
        envelopeKey: "locations",
        columns: [
          { key: "name", ar: "المنشأة", en: "Facility" },
          { key: "district", ar: "الحي/المركز", en: "District" },
          { key: "accuracyM", ar: "الدقة (م)", en: "Accuracy (m)" },
        ],
        demoRows: [{ "id": "gd1", "name": "مستشفى الاختبار المركزي", "district": "مدينة نصر", "accuracyM": 5 }],
      }}
    />
  );
}
