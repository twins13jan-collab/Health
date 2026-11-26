import ModuleScreen from "@/components/ModuleScreen";

export default function Page() {
  return (
    <ModuleScreen
      config={{
        titleKey: "donationCampaigns",
        subtitleAr: "حملات تبرع بمواقع وطاقة + أهلية متبرع تُفحص يومها وفاصل 90 يومًا لا يُكسر مهما فرغت الأرفف + نتائج تفاعلية تُبلَّغ للمتبرع بسرية مع إحالة ولا تظهر في تقارير الحملة + توزيع وحدات ضد الطلب وفي حدود المجمع فعليًا",
        subtitleEn: "Donation campaigns with sites and capacity + donor eligibility checked on the day and a 90-day interval never broken however empty the shelves + reactive results communicated confidentially to the donor with a referral and never shown in campaign reports + units allocated against demand and never beyond what was collected",
        endpoint: "/api/donation-campaigns/campaigns",
        envelopeKey: "campaigns",
        columns: [
          { key: "name", ar: "الحملة", en: "Campaign" },
          { key: "site", ar: "الموقع", en: "Site" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
