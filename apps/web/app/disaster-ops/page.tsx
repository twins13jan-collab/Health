import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "disasterOps",
        subtitleAr: "غرفة عمليات قومية ومراكز محافظات ومنشآت + تصنيف حدث قبل تفعيل الخطط — الاستجابة بحجم التصنيف لا الضجيج + صورة موقف موحدة: إصابات وأسرة وفرق ومخزون وطرق + موارد تُطلب وتُخصص بقرار موثق بسند صلاحية وتُسلَّم ضده + لا إغلاق وطلب لم يُسلَّم",
        subtitleEn: "A national operations room with governorate and facility centres + event classification before plan activation — response scales to the classification, not the noise + one common situation picture: casualties, beds, teams, stock and roads + resources requested, allocated by a logged decision with an authority basis, and delivered against it + no closure while a request is undelivered",
        endpoint: "/api/disaster-ops/events",
        envelopeKey: "events",
        columns: [
          { key: "name", ar: "الحدث", en: "Event" },
          { key: "classification", ar: "التصنيف", en: "Classification" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
        demoRows: [{ "id": "do1", "name": "فيضان موسمي", "classification": 2, "status": "responding" }],
      }}
    />
  );
}
