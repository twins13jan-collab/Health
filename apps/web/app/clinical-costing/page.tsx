import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "clinicalCosting",
        subtitleAr: "حساب تكلفة مباشرة وغير مباشرة لخدمة وإقامة ودواء مربوطة بالاستهلاك الفعلي فقط — المخطط غير المنفذ لا يكلف شيئًا في هذا السجل + تحليل تكلفة مسار عبر الأقسام وانحراف عن معيارية بمراجعة عند تجاوز 25% + قياس قيمة دون اختزال القرار الطبي بالتكلفة: مراجعة الانحراف يغلقها سريري لا المالية وحدها",
        subtitleEn: "Direct and indirect cost of service, stay and medication linked to actual consumption only — what was planned but never executed costs nothing in this ledger + pathway cost analysed across departments with variance review beyond 25% from standard + value measured without reducing the medical decision to its cost: a variance review is closed by a clinician, never by finance alone",
        endpoint: "/api/clinical-costing/cases",
        envelopeKey: "cases",
        columns: [
          { key: "pathwayRef", ar: "المسار", en: "Pathway" },
          { key: "standardCost", ar: "التكلفة المعيارية", en: "Standard cost" },
          { key: "varianceReviewStatus", ar: "مراجعة الانحراف", en: "Variance review" },
        ],
        demoRows: [{ "id": "cc1", "pathwayRef": "مسار قسطرة قلبية", "standardCost": 42000, "varianceReviewStatus": "none" }],
      }}
    />
  );
}
