"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState, type FormEvent } from "react";
import { api, ApiError } from "@/lib/api";
import { useLang } from "@/lib/i18n";
import FacilityCascade, { type FacilityRow } from "@/components/FacilityCascade";

/** Sign-in (v3) — a centered card on a soft green-tinted ground,
 *  inside the public shell. No split screen. */
export default function LoginPage() {
  const { lang } = useLang();
  const ar = lang === "ar";
  const router = useRouter();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const [scopeOpen, setScopeOpen] = useState(false);
  const [scopeFacility, setScopeFacility] = useState<FacilityRow | null>(null);

  const onSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      const res = await api.login(email.trim(), password, scopeFacility?.id);
      router.push(res.data.user.role === "patient" ? "/my" : "/dashboard");
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
    <div className="soft-bg">
      <div className="auth-wrap">
        <div className="auth-card">
          {/* eslint-disable-next-line @next/next/no-img-element */}
          <img src="/emblem.png" alt="خطاب" width={76} height={76} className="auth-emblem" />
          <h1>{ar ? "تسجيل الدخول" : "Sign in"}</h1>
          <p className="auth-sub">
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
            <div className="field">
              <button type="button" className="btn btn-sm" onClick={() => setScopeOpen(!scopeOpen)}>
                {ar ? "الدخول ضمن نطاق منشأة (اختياري)" : "Sign in under a facility scope (optional)"} {scopeOpen ? "▾" : "▸"}
              </button>
              {scopeOpen ? (
                <div className="mt">
                  <p className="muted">{ar
                    ? "اختر بالتسلسل: الجهة ← المحافظة ← المديرية ← نوع المنشأة ← اسم المنشأة. الحساب يجب أن يتبع المنشأة المختارة فعلًا — حسابات الإشراف تدخل بأي منشأة داخل نطاقها."
                    : "Pick in order: entity → governorate → directorate → facility type → facility name. The account must actually belong to the chosen facility — oversight accounts may enter under any facility in their remit."}</p>
                  <FacilityCascade onPick={setScopeFacility} />
                  {scopeFacility ? <p className="muted">{ar ? "النطاق المختار:" : "Selected scope:"} <strong>{scopeFacility.nameAr}</strong></p> : null}
                </div>
              ) : null}
            </div>
            {error ? <div className="alert-error" role="alert">{error}</div> : null}
            <button className="btn btn-block" type="submit" disabled={busy}>
              {busy ? "…" : ar ? "دخول" : "Sign in"}
            </button>
          </form>
          <p className="auth-links">
            <Link href="/register">{ar ? "إنشاء حساب جديد — مريض أو منشأة" : "Create an account — patient or facility"}</Link>
          </p>
          <p className="auth-links muted">
            <Link href="/download-app">📱 {ar ? "تحميل تطبيق الأندرويد" : "Download the Android app"}</Link>
          </p>
        </div>
      </div>
    </div>
  );
}
