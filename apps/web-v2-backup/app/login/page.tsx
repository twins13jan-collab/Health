"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState, type FormEvent } from "react";
import { api, ApiError } from "@/lib/api";
import { useLang } from "@/lib/i18n";

/** Sign-in — rendered inside PublicShell: a split brand/form card
 *  under the public website chrome. */
export default function LoginPage() {
  const { lang } = useLang();
  const ar = lang === "ar";
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
          ar
            ? "تعذّر الاتصال بالخادم — تحقق من الشبكة وحاول مجددًا"
            : "Cannot reach the server — check the connection and retry",
        );
      }
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="login-shell">
      <section className="login-brand">
        {/* eslint-disable-next-line @next/next/no-img-element */}
        <img src="/logo.png" alt="خطاب" className="login-logo" />
        <h1>{ar ? "خطاب للصحة العامة وإدارة الأزمات" : "Khattab for Public Health and Crisis Management"}</h1>
        <p>
          {ar
            ? "منظومة خطاب لإدارة الصحة العامة والأزمات والطوارئ الطبية والمنشآت الصحية"
            : "Khattab System for Public Health, Crisis, Medical Emergency and Health Facility Management"}
        </p>
      </section>
      <section className="login-form-card">
        <h2>{ar ? "تسجيل الدخول" : "Sign in"}</h2>
        <p className="login-hint">
          {ar
            ? "حسابات الطاقم تُنشأ بواسطة مسؤول النظام في منشأتك. المواطنون والمنشآت يمكنهم إنشاء حساب من صفحة التسجيل."
            : "Staff accounts are provisioned by your facility's system administrator. Citizens and facilities can register from the sign-up page."}
        </p>
        <form onSubmit={onSubmit}>
          <div className="field">
            <label htmlFor="email">{ar ? "البريد الإلكتروني الرسمي" : "Official email"}</label>
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
          <div className="field">
            <label htmlFor="password">{ar ? "كلمة المرور" : "Password"}</label>
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
            {busy ? "…" : ar ? "دخول" : "Sign in"}
          </button>
        </form>
        <p className="muted l-mt">
          <Link href="/register">إنشاء حساب جديد — مريض أو منشأة</Link>
        </p>
        <p className="login-hint text-center l-mt">
          <Link href="/download-app">📱 {ar ? "تحميل تطبيق الأندرويد" : "Download the Android app"}</Link>
        </p>
      </section>
    </div>
  );
}
