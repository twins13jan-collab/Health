"use client";

import { useRouter } from "next/navigation";
import { useState, type FormEvent } from "react";
import { api } from "@/lib/api";
import { useLang } from "@/lib/i18n";

export default function LoginPage() {
  const { lang, t } = useLang();
  const router = useRouter();
  const [form, setForm] = useState({ username: "", password: "", facilityCode: "" });
  const [msg, setMsg] = useState<{ ok: boolean; text: string } | null>(null);
  const [busy, setBusy] = useState(false);

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setBusy(true);
    setMsg(null);
    try {
      // OIDC-style: the core issues an OAuth 2.1 / OIDC access token via
      // POST /api/auth/login; MFA (TOTP) is enforced by the core when enabled.
      const r = await api.login(form);
      setMsg({
        ok: true,
        text: r.demo
          ? (lang === "ar" ? "تم الدخول في وضع العرض — الخادم الأساسي غير متاح" : "Signed in in demo mode — core API unreachable")
          : (lang === "ar" ? "تم تسجيل الدخول بنجاح" : "Signed in successfully"),
      });
      router.push("/");
    } catch {
      setMsg({ ok: false, text: lang === "ar" ? "بيانات الدخول غير صحيحة" : "Invalid credentials" });
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="login-wrap">
      <section className="card">
        <h2>{t("login")}</h2>
        <p style={{ color: "var(--ink-2)", marginTop: 0 }}>
          {lang === "ar"
            ? "دخول موحد OAuth 2.1 / OIDC مع تحقق متعدد العوامل (MFA) عند التفعيل"
            : "OAuth 2.1 / OIDC single sign-on, with MFA (TOTP) when enabled"}
        </p>
        <form onSubmit={onSubmit}>
          <div className="field" style={{ marginBottom: 12 }}>
            <label htmlFor="u">{lang === "ar" ? "اسم المستخدم / البريد الرسمي *" : "Username / official email *"}</label>
            <input id="u" required autoComplete="username" value={form.username} onChange={(e) => setForm({ ...form, username: e.target.value })} />
          </div>
          <div className="field" style={{ marginBottom: 12 }}>
            <label htmlFor="p">{lang === "ar" ? "كلمة المرور *" : "Password *"}</label>
            <input id="p" type="password" required autoComplete="current-password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} />
          </div>
          <div className="field" style={{ marginBottom: 16 }}>
            <label htmlFor="f">{lang === "ar" ? "كود المنشأة (اختياري)" : "Facility code (optional)"}</label>
            <input id="f" value={form.facilityCode} onChange={(e) => setForm({ ...form, facilityCode: e.target.value })} />
          </div>
          <button className="btn btn-block" type="submit" disabled={busy}>
            {busy ? "…" : t("login")}
          </button>
          {msg ? <div className={`form-msg ${msg.ok ? "ok" : "err"}`}>{msg.text}</div> : null}
        </form>
      </section>
    </div>
  );
}
