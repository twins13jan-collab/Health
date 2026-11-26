"use client";
import { useLang } from "@/lib/i18n";
import { SectionHead } from "@/components/ui";

/** من نحن — About the Khattab system: what it is, what it operates,
 *  and the principles it runs on. */
export default function AboutPage() {
  const { lang } = useLang();
  const ar = lang === "ar";
  return (
    <>
      <SectionHead eyebrow={ar ? "المنظومة" : "The system"} title={ar ? "من نحن" : "About Us"} />
      <div className="card">
        <div className="prose">
          {ar ? (<>
            <h2>ما هي منظومة خطاب؟</h2>
            <p>«خطاب للصحة العامة وإدارة الأزمات» منظومة تشغيل متكاملة لإدارة الصحة العامة والأزمات والطوارئ الطبية والمنشآت الصحية. ليست طبقة ربط فوق أنظمة أخرى، بل هي نظام التشغيل والسجل الأساسي الذي تعتمد عليه المنشأة في عملها اليومي: من استقبال المريض وقبوله، مرورًا بالرعاية والتشخيص والعلاج، وحتى الخروج والفوترة والمتابعة.</p>
            <h2>ماذا تدير المنظومة؟</h2>
            <ul>
              <li><strong>المستشفيات</strong> بكل أقسامها وإداراتها: الاستقبال والقبول والخروج والتحويلات، الطوارئ والفرز، العيادات الخارجية، الأجنحة ولوحة الأسرّة، التمريض، العمليات والتخدير، العناية المركزة والحضانات، الأمومة، الأشعة، السجلات الطبية، التعقيم المركزي، الخدمات المساندة، الصيانة الطبية، المالية والتأمين، الموارد البشرية، والمخازن والمشتريات.</li>
              <li><strong>المعامل</strong> بنظام معلومات مختبرات كامل: استقبال العينات والباركود، قوائم العمل، مراقبة الجودة الداخلية، اعتماد النتائج وإصدارها، القيم الحرجة وإخطارها، وتقارير زمن الإنجاز.</li>
              <li><strong>الصيدليات</strong>: طابور الوصفات والتحقق السريري، الصرف بالتشغيلات والأقدم انتهاءً، تنبيهات المخزون والصلاحية، سجل المواد المخدرة والمؤثرات العقلية، المرتجعات والهالك، وطلبات التموين.</li>
              <li><strong>المراكز والوحدات الصحية</strong>: ملفات طب الأسرة، صحة الأم والطفل ومتابعة الحمل، التطعيمات وسلسلة التبريد، عيادات الأمراض المزمنة، الصحة المدرسية، والإحالة للمستوى الأعلى ومتابعتها.</li>
              <li><strong>بنوك الدم وخدمات الطوارئ</strong>: تتبع كامل لوحدات الدم من التبرع حتى النقل، وإرسال الإسعاف ومتابعة السعة لحظيًا وتنسيق الإحالات وإدارة الحوادث الكبرى.</li>
            </ul>
            <h2>مبادئ التشغيل</h2>
            <ul>
              <li><strong>سجل موحد للمريض</strong> يتبعه عبر كل المنشآت والمحافظات.</li>
              <li><strong>دورات عمل موثقة</strong> بحالات وانتقالات محددة — 38 دورة تشغيل مرقمة تغطي العمل السريري والإداري كله.</li>
              <li><strong>تدقيق كامل</strong>: كل إجراء وكل انتقال يُسجَّل بفاعله ووقته وسببه.</li>
              <li><strong>عزل كامل بين المنشآت</strong>: كل منشأة ترى بياناتها فقط، والتجميع الوطني للقيادة والمؤشرات فقط.</li>
              <li><strong>دعم قرار لا استبدال للإنسان</strong>: التحليلات والتنبؤات استشارية، والقرار السريري للطبيب دائمًا.</li>
              <li><strong>عمل ميداني دون اتصال</strong> مع مزامنة آمنة عند عودة الشبكة.</li>
            </ul>
            <h2>النطاق</h2>
            <p>145 وحدة تشغيلية متكاملة، و38 دورة عمل موثقة، في نطاق 27 محافظة — بتشغيل على مدار الساعة للطوارئ والأزمات.</p>
          </>) : (<>
            <h2>What is Khattab?</h2>
            <p>“Khattab for Public Health and Crisis Management” is an integrated operating system for public health, crises, medical emergencies and health facilities. It is not an integration layer over other systems — it is the operating system and system of record each facility runs its daily work on: from patient reception and admission, through care, diagnosis and treatment, to discharge, billing and follow-up.</p>
            <h2>What does it operate?</h2>
            <ul>
              <li><strong>Hospitals</strong>, every department and administration: reception, admissions, discharges and transfers, emergency and triage, outpatient clinics, wards and the live bed board, nursing, theatres and anaesthesia, intensive care and nurseries, maternity, radiology, medical records, central sterilisation, support services, biomedical engineering, finance and insurance, human resources, stores and procurement.</li>
              <li><strong>Laboratories</strong> with a full LIS: sample reception and barcoding, worklists, internal quality control, result validation and release, critical values and their notification, and turnaround reporting.</li>
              <li><strong>Pharmacies</strong>: prescription queues and clinical verification, batch/FEFO dispensing, stock and expiry alerts, narcotics and psychotropics registers, returns and waste, and supply requests.</li>
              <li><strong>Health centres and units</strong>: family health files, maternal and child health and pregnancy follow-up, immunisation and cold chain, chronic-disease clinics, school health, and upward referral with follow-up.</li>
              <li><strong>Blood banks and emergency services</strong>: full tracing of blood units from donation to transfusion, ambulance dispatch, live capacity, referral coordination and major-incident management.</li>
            </ul>
            <h2>Operating principles</h2>
            <ul>
              <li><strong>One unified patient record</strong> that follows the patient across facilities and governorates.</li>
              <li><strong>Documented workflows</strong> with defined states and transitions — 38 numbered operating cycles covering all clinical and administrative work.</li>
              <li><strong>Complete audit</strong>: every action and transition is recorded with its actor, time and reason.</li>
              <li><strong>Full isolation between facilities</strong>: each facility sees only its own data; national aggregation is for leadership and indicators only.</li>
              <li><strong>Decision support, never a replacement for humans</strong>: analytics and forecasts are advisory; the clinical decision always belongs to the physician.</li>
              <li><strong>Offline field work</strong> with safe synchronisation when the network returns.</li>
            </ul>
            <h2>Scope</h2>
            <p>145 integrated operational modules and 38 documented operating cycles, across 27 governorates — with round-the-clock emergency and crisis operations.</p>
          </>)}
        </div>
      </div>
    </>
  );
}
