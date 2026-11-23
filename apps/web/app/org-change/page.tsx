import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "orgChange",
        subtitleAr: "تحليل أثر التغيير على الأدوار والسياسات والبيانات + الإطلاق محكوم: تواصل منجز وتدريب 80% من المتأثرين وإجراءات وتفويضات محدثة + سفراء تغيير وملاحظات مستخدمين معالجة + التبني يُقاس من الاستخدام الفعلي والحلول الجانبية لا يُعلن من تاريخ الإطلاق",
        subtitleEn: "Change impact analyzed across roles, policies and data + go-live is gated: communication done, 80% of affected users trained, procedures and delegations updated + change champions and addressed user feedback + adoption is measured from actual use and workaround reports, never declared from the launch date",
        endpoint: "/api/org-change/initiatives",
        envelopeKey: "initiatives",
        columns: [
          { key: "title", ar: "المبادرة", en: "Initiative" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
        demoRows: [{ "id": "oc1", "title": "إطلاق نظام الاستقبال الجديد", "status": "live" }],
      }}
    />
  );
}
