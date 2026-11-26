"use client";

import { useLang } from "@/lib/i18n";
import { PublicHeader, PublicFooter, PublicSideMenu } from "@/components/PublicChrome";

/**
 * Official in-system distribution point for the Khattab Android app.
 * Public on purpose: field staff install the app before they sign in.
 * The APK itself is served from /downloads/khattab-health.apk (version
 * metadata in /downloads/version.json is the update source of truth).
 */
export default function DownloadAppPage() {
  const { lang } = useLang();
  const ar = lang === "ar";

  return (
    <>
      <PublicHeader />
      <div className="landing">
        <div className="page-grid">
          <PublicSideMenu />
          <div>
      <div className="page-head">
        <h1>{ar ? "تطبيق خطاب للأندرويد" : "Khattab Android App"}</h1>
        <p>
          {ar
            ? "قناة التوزيع الرسمية الداخلية — ثبّت التطبيق على هاتف العمل وادخل بنفس حسابك في المنظومة"
            : "The official internal distribution channel — install on your work phone and sign in with your system account"}
        </p>
      </div>

      <section className="card" style={{ display: "flex", gap: 22, flexWrap: "wrap", alignItems: "center" }}>
        <img src="/emblem.png" alt="خطاب" width={92} height={92} style={{ borderRadius: 18 }} />
        <div style={{ flex: "1 1 260px" }}>
          <h2 style={{ marginBottom: 6 }}>{ar ? "خطاب للصحة العامة وإدارة الأزمات" : "Khattab for Public Health and Crisis Management"}</h2>
          <p style={{ margin: 0, color: "var(--ink-2)" }}>
            {ar ? "الإصدار 6.0.2 · الحجم ٢٠٫٥ ميجابايت · أندرويد ٥٫٠ فأعلى · الحزمة com.khattab.health" : "Version 6.0.2 · 20.5 MB · Android 5.0+ · com.khattab.health"}
          </p>
          <p style={{ margin: "6px 0 0", color: "var(--ink-2)" }}>
            {ar ? "متصل مباشرة بخادم الإنتاج khattab-ai.com" : "Connected directly to the production server khattab-ai.com"}
          </p>
        </div>
        <a className="btn" href="/downloads/khattab-health.apk" download style={{ fontSize: 16, padding: "12px 26px" }}>
          ⬇ {ar ? "تحميل التطبيق (APK)" : "Download the app (APK)"}
        </a>
      </section>

      <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(300px, 1fr))", gap: 18 }}>
        <section className="card">
          <h2>{ar ? "خطوات التثبيت" : "Install steps"}</h2>
          <ol style={{ margin: 0, paddingInlineStart: 20, lineHeight: 2 }}>
            <li>{ar ? "اضغط زر التحميل بالأعلى من متصفح الهاتف" : "Tap the download button above in the phone browser"}</li>
            <li>{ar ? "افتح الملف khattab-health.apk بعد اكتمال التحميل" : "Open khattab-health.apk once the download finishes"}</li>
            <li>{ar ? "لو طلب الهاتف إذنًا، فعّل «السماح من هذا المصدر» لمتصفحك ثم ارجع للتثبيت" : "If asked, allow “install from this source” for your browser, then resume"}</li>
            <li>{ar ? "افتح التطبيق وادخل بنفس بريدك وكلمة مرورك في المنظومة" : "Open the app and sign in with your system email and password"}</li>
          </ol>
        </section>
        <section className="card" style={{ textAlign: "center" }}>
          <h2>{ar ? "امسح بالهاتف" : "Scan with your phone"}</h2>
          <img src="/downloads/download-qr.png" alt="QR" width={210} height={210} style={{ border: "1px solid var(--line)", borderRadius: 12 }} />
          <p style={{ color: "var(--ink-2)", margin: "10px 0 0" }}>
            {ar ? "الكود يفتح صفحة التحميل هذه على هاتفك مباشرة" : "The code opens this download page on your phone"}
          </p>
        </section>
      </div>

      <section className="card">
        <h2>{ar ? "التحديثات" : "Updates"}</h2>
        <p style={{ margin: 0, color: "var(--ink-2)", lineHeight: 1.9 }}>
          {ar
            ? "الإصدارات الجديدة تُنشر هنا بنفس الرابط — عند صدور تحديث، أعد التحميل من هذه الصفحة وثبّت فوق الإصدار الحالي دون حذف بياناتك. بيانات الإصدار الحالي متاحة أيضًا في /downloads/version.json للتحقق الآلي."
            : "New releases are published here at the same link — when an update ships, download again from this page and install over the current version without losing your data. Current release metadata is also at /downloads/version.json for automated checks."}
        </p>
      </section>
          </div>
        </div>
      </div>
      <PublicFooter />
    </>
  );
}
