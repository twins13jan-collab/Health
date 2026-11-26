"use client";
import Link from "next/link";
import { useLang } from "@/lib/i18n";

/** السياسة والخصوصية — the system's policy & privacy page (v1.0). */
export default function PrivacyPage() {
  const { lang } = useLang();
  const ar = lang === "ar";
  return (
    <>
      <div className="card">
        <h1 className="first">{ar ? "السياسة والخصوصية" : "Policy & Privacy"}</h1>
        <p className="muted">
          {ar ? "الإصدار 1.0 — نافذة من 23 نوفمبر 2026. تسري هذه السياسة على موقع ومنظومة وتطبيق «خطاب للصحة العامة وإدارة الأزمات»." : "Version 1.0 — effective 23 November 2026. This policy applies to the website, platform and app of “Khattab for Public Health and Crisis Management”."}
        </p>
        <div className="prose">
          {ar ? (<>
            <h2>1. البيانات التي تعالجها المنظومة</h2>
            <p>تعالج المنظومة بيانات المرضى الصحية والإدارية اللازمة لتقديم الرعاية وتشغيل المنشآت: بيانات الهوية والتسجيل، اللقاءات والتشخيصات والنتائج والوصفات، وبيانات التشغيل (السعة، الأسرّة، الحركات المخزنية والمالية)، وسجلات تدقيق لكل إجراء. لا تجمع المنظومة أي بيانات لأغراض إعلانية ولا تبيع بيانات لأي طرف.</p>
            <h2>2. الغرض من المعالجة</h2>
            <p>تقديم الرعاية الصحية وتشغيل المنشآت، وتنسيق الطوارئ والإحالات، وإصدار التقارير والمؤشرات الوطنية بصورة مجمعة، والوفاء بالالتزامات التنظيمية والمحاسبية. أي استخدام آخر يتطلب أساسًا مستقلًا وموثقًا.</p>
            <h2>3. عزل المنشآت والصلاحيات</h2>
            <p>بيانات كل منشأة معزولة تقنيًا عن غيرها على مستوى قاعدة البيانات نفسها؛ لا يرى أي مستخدم إلا بيانات منشأته ونطاقه، وفق مصفوفة صلاحيات حسب الدور. التجميع الوطني يظهر للقيادات المخولة في صورة مؤشرات وملخصات، ولا يُستخدم للاطلاع على سجلات مرضى منشآت أخرى إلا لغرض رعاية موثق (كالإحالة) وبتدقيق كامل.</p>
            <h2>4. التدقيق والمساءلة</h2>
            <p>كل دخول وكل إجراء وكل انتقال في دورات العمل يُسجَّل بفاعله ووقته وسببه في سجل تدقيق لا يُعدَّل. الاطلاع على سجل مريض يُسجَّل هو الآخر.</p>
            <h2>5. الأمان</h2>
            <p>حسابات فردية بكلمات مرور قوية وسياسة قفل عند تكرار المحاولات الفاشلة، ونقل مشفر (HTTPS)، وتشفير للبيانات الحساسة، ونسخ احتياطية مجدولة. الحسابات تُنشأ وتُدار بمعرفة مسؤولي النظام فقط؛ لا يوجد تسجيل ذاتي مفتوح.</p>
            <h2>6. الاحتفاظ والحذف</h2>
            <p>تُحفظ السجلات الطبية وفق مدد الاحتفاظ التنظيمية للسجلات الصحية، وتُحذف أو تُجهَّل بعدها وفق سياسة الاحتفاظ والأرشفة الموثقة في المنظومة. سجلات التدقيق تُحفظ لمدة أطول لأغراض المساءلة.</p>
            <h2>7. مشاركة البيانات</h2>
            <p>لا تُشارك بيانات المرضى مع أطراف خارجية إلا في حدود تقديم الرعاية (إحالة موثقة بين منشأتين)، أو التزام قانوني، وبحد أدنى من البيانات وبسجل تدقيق. الصفحة الرئيسية العامة تعرض مؤشرات مجمعة فقط (أعداد وسعات ونسب إشغال) ولا تعرض أي بيانات تخص مريضًا بعينه.</p>
            <h2>8. حقوق أصحاب البيانات</h2>
            <p>للمريض حق الاطلاع على سجله وطلب تصحيحه وفق الإجراءات التنظيمية، وحق معرفة من اطّلع عليه من سجل التدقيق. تُقدَّم الطلبات عبر المنشأة المعالجة أو قناة التواصل الرسمية.</p>
            <h2>9. التحليلات والذكاء الاصطناعي</h2>
            <p>مخرجات التحليلات والتنبؤ داخل المنظومة استشارية لدعم القرار ولا تُنفِّذ أي إجراء سريري تلقائيًا؛ وعند استخدام أي خدمة ذكاء خارجية مستقبلًا تُزال معرفات المرضى قبل أي إرسال.</p>
            <h2>10. ملفات تعريف الارتباط</h2>
            <p>يستخدم الموقع التخزين المحلي للمتصفح فقط لحفظ جلسة الدخول واللغة المفضلة. لا تُستخدم ملفات تتبع إعلانية.</p>
            <h2>11. التواصل بشأن الخصوصية</h2>
            <p>لأي استفسار عن هذه السياسة أو عن بياناتك، تواصل عبر صفحة <Link href="/contact">التواصل</Link>.</p>
            <h2>12. تعديل السياسة</h2>
            <p>قد تُحدَّث هذه السياسة مع تطور المنظومة؛ يُنشر كل إصدار جديد هنا برقمه وتاريخ نفاذه.</p>
          </>) : (<>
            <h2>1. Data the system processes</h2>
            <p>The system processes the patient health and administrative data needed to deliver care and operate facilities: identity and registration data, encounters, diagnoses, results and prescriptions, operational data (capacity, beds, stores and financial movements), and an audit record of every action. The system collects no data for advertising and sells no data to anyone.</p>
            <h2>2. Purpose</h2>
            <p>Delivering healthcare and operating facilities, coordinating emergencies and referrals, producing national reports and indicators in aggregate form, and meeting regulatory and accounting obligations. Any other use requires its own documented basis.</p>
            <h2>3. Facility isolation and access control</h2>
            <p>Each facility's data is technically isolated from every other facility at the database level itself; every user sees only their own facility and scope, under a role-based permission matrix. National aggregation is shown to authorised leadership as indicators and summaries, and is never a route to browse another facility's patient records except for a documented care purpose (such as a referral), fully audited.</p>
            <h2>4. Audit and accountability</h2>
            <p>Every sign-in, action and workflow transition is recorded with its actor, time and reason in an immutable audit log. Viewing a patient's record is itself recorded.</p>
            <h2>5. Security</h2>
            <p>Individual accounts with strong passwords and lockout on repeated failed attempts, encrypted transport (HTTPS), encryption of sensitive data, and scheduled backups. Accounts are created and managed by system administrators only; there is no open self-registration.</p>
            <h2>6. Retention and deletion</h2>
            <p>Medical records are kept for the regulatory retention periods for health records, then deleted or anonymised under the system's documented retention and archiving policy. Audit logs are kept longer for accountability.</p>
            <h2>7. Data sharing</h2>
            <p>Patient data is shared with external parties only within care delivery (a documented referral between two facilities) or a legal obligation, with the minimum necessary data and a full audit record. The public homepage shows aggregate indicators only (counts, capacities and occupancy ratios) and never any individual patient's data.</p>
            <h2>8. Data-subject rights</h2>
            <p>Patients may access their record and request its correction under the regulatory procedures, and may know who viewed it from the audit log. Requests go through the treating facility or the official contact channel.</p>
            <h2>9. Analytics and AI</h2>
            <p>Analytics and forecasting outputs inside the system are advisory decision support and never execute a clinical action automatically; if any external AI service is used in future, patient identifiers are removed before any transmission.</p>
            <h2>10. Cookies</h2>
            <p>The site uses browser local storage only to keep the sign-in session and the preferred language. No advertising trackers are used.</p>
            <h2>11. Privacy contact</h2>
            <p>For any question about this policy or your data, use the <Link href="/contact">Contact</Link> page.</p>
            <h2>12. Changes</h2>
            <p>This policy may be updated as the system evolves; every new version is published here with its number and effective date.</p>
          </>)}
        </div>
      </div>
    </>
  );
}
