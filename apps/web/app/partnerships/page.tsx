import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "partnerships",
        subtitleAr: "سجل جهات ومقاولين ومقدمي خدمات + العقد لا ينشط إلا بشروط خصوصية وأمن ونطاق وصول وغرض ومدة صالحة + منح الوصول محددة الغرض والمدة وتحت عقد نشط فقط + إنهاء العلاقة يلغي كل الوصول فورًا + مراجعة أداء وحوادث وتدقيق ضد مستوى الخدمة وتضارب المصالح وتغير الملكية على السجل",
        subtitleEn: "Registry of partners, contractors and providers + a contract activates only with privacy and security terms, a defined access scope and purpose, and a valid period + access grants are purpose-bound and time-boxed under an active contract only + termination revokes all access immediately + performance, incidents and audit reviewed against the SLA, with conflicts and ownership changes on record",
        endpoint: "/api/partnerships/",
        envelopeKey: "partners",
        columns: [
          { key: "name", ar: "الجهة", en: "Partner" },
          { key: "partnerType", ar: "النوع", en: "Type" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
        demoRows: [{ "id": "pn1", "name": "شركة صيانة أجهزة", "partnerType": "contractor", "status": "active" }],
      }}
    />
  );
}
