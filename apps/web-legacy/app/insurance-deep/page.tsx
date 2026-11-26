import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "insuranceDeep",
        subtitleAr: "جهات تغطية ومنافع واستثناءات + تحقق أهلية وموافقات مسبقة + مطالبات وترميز وتدقيق ورفض واستئناف بمطابقة خدمة منفذة مع توثيق وتعريفة + تسويات ومدفوعات واستردادات — والضرورة الطبية تُسجل مستقلة عن قرار التغطية فرفض الدافع لا يعيد كتابتها، وإشارة الاحتيال يراجعها إنسان قبل أن توقف تسوية: الإشارة اشتباه لا حكم",
        subtitleEn: "Coverage providers, benefits and exclusions + eligibility and pre-authorization checks + claims, coding, audit, rejection and appeals with the performed service matched against documentation and tariff + settlements, payments and recoveries — medical necessity is recorded independently of the coverage decision, which never rewrites it, and a fraud signal is reviewed by a human before it can stop a settlement: a flag is a suspicion, not a verdict",
        endpoint: "/api/insurance-deep/claims",
        envelopeKey: "claims",
        columns: [
          { key: "patientRef", ar: "المريض", en: "Patient" },
          { key: "serviceRef", ar: "الخدمة", en: "Service" },
          { key: "coverageDecision", ar: "قرار التغطية", en: "Coverage decision" },
        ],
      }}
    />
  );
}
