"use client";
import { useLang } from "@/lib/i18n";
import { PublicHeader, PublicFooter, PublicSideMenu } from "@/components/PublicChrome";

/** التواصل — Contact. The official contact is Ahmed Khattab, by
 *  phone and WhatsApp on +20 155 662 6662 (as directed by the owner). */
export default function ContactPage() {
  const { lang } = useLang();
  const ar = lang === "ar";
  return (
    <>
      <PublicHeader />
      <div className="landing">
        <div className="page-grid">
          <PublicSideMenu />
          <div>
      <h1 className="section-title" style={{ marginTop: 0 }}>{ar ? "التواصل" : "Contact"}</h1>
      <p className="section-sub">
        {ar
          ? "للتواصل بشأن تشغيل المنظومة أو انضمام منشأة صحية أو الدعم — تواصل مباشرة:"
          : "For operating the system, onboarding a health facility, or support — contact directly:"}
      </p>
      <div className="contact-grid">
        <div className="card">
          <div className="landing-feature-icon">👤</div>
          <h3 style={{ margin: "0 0 6px" }}>{ar ? "مسؤول التواصل" : "Contact person"}</h3>
          <div className="contact-value">{ar ? "أحمد خطاب" : "Ahmed Khattab"}</div>
        </div>
        <div className="card">
          <div className="landing-feature-icon">📞</div>
          <h3 style={{ margin: "0 0 6px" }}>{ar ? "الهاتف" : "Phone"}</h3>
          <div className="contact-value"><a href="tel:+201556626662" dir="ltr">+20 155 662 6662</a></div>
          <p className="muted" style={{ margin: "8px 0 0" }}>{ar ? "اتصال مباشر" : "Direct call"}</p>
        </div>
        <div className="card">
          <div className="landing-feature-icon">💬</div>
          <h3 style={{ margin: "0 0 6px" }}>{ar ? "واتساب" : "WhatsApp"}</h3>
          <div className="contact-value">
            <a href="https://wa.me/201556626662" target="_blank" rel="noreferrer" dir="ltr">+20 155 662 6662</a>
          </div>
          <p className="muted" style={{ margin: "8px 0 0" }}>{ar ? "اضغط الرقم لبدء محادثة واتساب" : "Tap the number to start a WhatsApp chat"}</p>
        </div>
        <div className="card">
          <div className="landing-feature-icon">🌐</div>
          <h3 style={{ margin: "0 0 6px" }}>{ar ? "الموقع الرسمي" : "Official website"}</h3>
          <div className="contact-value"><a href="https://khattab-ai.com" dir="ltr">khattab-ai.com</a></div>
          <p className="muted" style={{ margin: "8px 0 0" }}>{ar ? "المنظومة وتطبيق الأندرويد من داخل الموقع" : "The system and the Android app, from this site"}</p>
        </div>
      </div>
          </div>
        </div>
      </div>
      <PublicFooter />
    </>
  );
}
