import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "purchasing",
        subtitleAr: "سلسلة كاملة من الاحتياج إلى الدفع بفصل مهام في كل خطوة + مطابقة ثلاثية قبل أي صرف والفاتورة المخالفة تعلق + تغيير بنك المورد بتحقق مستقل + تعديل العقد ملحق بإصدار جديد لا يمحو السابق",
        subtitleEn: "A full chain from need to payment with duties separated at every step + three-way matching before any payment and mismatching invoices are held + supplier bank changes need independent verification + contract amendments are new addendum versions that never erase the previous",
        endpoint: "/api/purchasing/requests",
        envelopeKey: "requests",
        columns: [
          { key: "itemDescription", ar: "الاحتياج", en: "Need" },
          { key: "quantity", ar: "الكمية", en: "Qty" },
          { key: "requester", ar: "الطالب", en: "Requester" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
        demoRows: [{ "id": "pu1", "itemDescription": "أجهزة مراقبة علامات حيوية", "quantity": 4, "requester": "العناية المركزة", "status": "approved" }],
      }}
    />
  );
}
