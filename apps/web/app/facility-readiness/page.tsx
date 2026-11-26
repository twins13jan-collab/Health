import ModuleScreen from "@/components/ModuleScreen";

export default function Page() {
  return (
    <ModuleScreen
      config={{
        titleKey: "facilityReadiness",
        subtitleAr: "تحقق من اكتمال تراخيص وكوادر وأجهزة ومخزون وبنية تحتية وبروتوكولات وتدريب ومسارات إحالة — الفحوص الثمانية معًا + ملاحظات وعوائق وخطة معالجة: عائق بلا خطة شكوى لا إدارة + قرار تفعيل خدمة بصاحب صلاحية مسمى وإعادة تحقق عند تغير قدرة أو عطل وإظهار حالة جاهزية وحدودها ومصدرها",
        subtitleEn: "Completeness of licences, staff, equipment, stock, infrastructure, protocols, training and referral paths verified — all eight checks together + findings, blockers and a remediation plan: a blocker without a plan is a complaint, not management + the service-activation decision by a named authorized owner, re-verification when capability changes or a failure occurs, and the readiness state, its limits and its source shown",
        endpoint: "/api/facility-readiness/assessments",
        envelopeKey: "assessments",
        columns: [
          { key: "serviceRef", ar: "الخدمة", en: "Service" },
          { key: "decision", ar: "القرار", en: "Decision" },
          { key: "staffOk", ar: "الكوادر مكتملة", en: "Staff complete" },
        ],
        rowActions: [
          { labelAr: "تفعيل الخدمة", labelEn: "Activate service", endpoint: "/api/facility-readiness/assessments/{id}/activate", body: { decidedBy: "مدير المنشأة" } },
          { labelAr: "تسجيل غير جاهز", labelEn: "Record not ready", endpoint: "/api/facility-readiness/assessments/{id}/record-not-ready", body: { decidedBy: "مدير المنشأة" } },
        ],
      }}
    />
  );
}
