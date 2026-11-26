"use client";

import { useRouter } from "next/navigation";
import { useState, type FormEvent } from "react";
import { api, ApiError } from "@/lib/api";
import { useLang } from "@/lib/i18n";
import { PublicHeader, PublicFooter } from "@/components/PublicChrome";

export default function LoginPage() {
  const { lang } = useLang();
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      await api.login(email.trim(), password);
      router.push("/dashboard");
    } catch (err) {
      if (err instanceof ApiError) {
        setError(err.message);
      } else {
        setError(
          lang === "ar"
            ? "تعذّر الاتصال بالخادم — تحقق من الشبكة وحاول مجددًا"
            : "Cannot reach the server — check the connection and retry",
        );
      }
    } finally {
      setBusy(false);
    }
  };

  return (
    <>
      <PublicHeader />
      <div className="login-shell">
      <section className="login-brand">
        <img src="/logo.png" alt="خطاب" className="login-logo" />
        <h1>{lang === "ar" ? "خطاب للصحة العامة وإدارة الأزمات" : "Khattab for Public Health and Crisis Management"}</h1>
        <p>
          {lang === "ar"
            ? "منظومة خطاب لإدارة الصحة العامة والأزمات والطوارئ الطبية والمنشآت الصحية"
            : "Khattab System for Public Health, Crisis, Medical Emergency and Health Facility Management"}
        </p>
      </section>
      <section className="login-form-card">
        <h2>{lang === "ar" ? "تسجيل الدخول" : "Sign in"}</h2>
        <p className="login-hint">
          {lang === "ar"
            ? "حسابات الطاقم تُنشأ بواسطة مسؤول النظام في منشأتك. المواطنون والمنشآت يمكنهم إنشاء حساب من صفحة التسجيل."
            : "Staff accounts are provisioned by your facility's system administrator. Citizens and facilities can register from the sign-up page."}
        </p>
        <form onSubmit={onSubmit}>
          <div className="field" style={{ marginBottom: 12 }}>
            <label htmlFor="email">{lang === "ar" ? "البريد الإلكتروني الرسمي" : "Official email"}</label>
            <input
              id="email"
              type="email"
              required
              autoComplete="username"
              dir="ltr"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
          </div>
          <div className="field" style={{ marginBottom: 16 }}>
            <label htmlFor="password">{lang === "ar" ? "كلمة المرور" : "Password"}</label>
            <input
              id="password"
              type="password"
              required
              autoComplete="current-password"
              dir="ltr"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
          </div>
          {error ? <div className="error-banner" role="alert">{error}</div> : null}
          <button className="btn btn-block" type="submit" disabled={busy}>
            {busy ? "…" : lang === "ar" ? "دخول" : "Sign in"}
          </button>
        </form>
          <p className="muted" style={{ marginTop: 12 }}><a href="/register">إنشاء حساب جديد — مريض أو منشأة</a></p>
        <p className="login-hint" style={{ marginTop: 18, textAlign: "center" }}>
          <a href="/download-app">📱 {lang === "ar" ? "تحميل تطبيق الأندرويد" : "Download the Android app"}</a>
        </p>
      </section>
      </div>
      <PublicFooter />
    </>
  );
}
