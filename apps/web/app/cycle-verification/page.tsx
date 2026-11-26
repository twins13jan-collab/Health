import ModuleScreen from "@/components/ModuleScreen";

export default function Page() {
  return (
    <ModuleScreen
      config={{
        titleKey: "cycleVerification",
        subtitleAr: "تعريف بدء خدمة واكتمالها ومسؤول كل انتقال + تحقق من طلب وفرز وموافقة وتنفيذ ونتيجة ومتابعة بأدلة مرجعية + تحقق من دورة دواء ومخزون وفاتورة وتكلفة وقيد وقبول إحالة وتقديم خدمة وإرجاع نتيجة + إدارة حالات معلقة ومتعثرة وملغاة علنًا — والشاشة أو استجابة API ليستا دليل اكتمال عملية أبدًا كما ينص الكتالوج",
        subtitleEn: "The start of a service, its completion and the owner of every transition defined + request, triage, approval, execution, result and follow-up verified with evidence references + the medication, stock, invoice, cost and journal cycle and referral acceptance, delivery and result return checked + stuck, stalled and cancelled cases managed in the open — a screen or an API response is never evidence the process completed, as the catalog states outright",
        endpoint: "/api/cycle-verification/verifications",
        envelopeKey: "verifications",
        columns: [
          { key: "cycleRef", ar: "الدورة", en: "Cycle" },
          { key: "instanceRef", ar: "الحالة", en: "Instance" },
          { key: "followupDone", ar: "المتابعة تمت", en: "Follow-up done" },
        ],
      }}
    />
  );
}
