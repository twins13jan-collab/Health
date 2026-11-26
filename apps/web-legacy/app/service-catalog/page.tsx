import ModulePage from "@/components/ModulePage";

export default function Page() {
  return (
    <ModulePage
      config={{
        titleKey: "serviceCatalog",
        subtitleAr: "كتالوج قومي للخدمات والإجراءات والفحوصات والتخصصات ومحددات الإتاحة — رمز واحد وتعريف واحد، وكل خدمة مربوطة بمتطلبات مواردها وكوادرها وأجهزتها ومستهلكاتها ومكان تقديمها، والأسعار والتغطيات والعقود إدارية منفصلة عن القرار الطبي، والتغيير باعتماد مسمى وتاريخ سريان لا بأثر رجعي خفي",
        subtitleEn: "A national catalog of services, procedures, tests, specialties and availability determinants — one code, one definition, every service linked to its resource, staff, device and consumable requirements and delivery location, prices, coverage and contracts kept administrative and separate from the clinical decision, and changes by named approval and effective date, never silent retroactivity",
        endpoint: "/api/service-catalog/services",
        envelopeKey: "services",
        columns: [
          { key: "code", ar: "الرمز", en: "Code" },
          { key: "name", ar: "الخدمة", en: "Service" },
          { key: "status", ar: "الحالة", en: "Status", status: true },
        ],
      }}
    />
  );
}
