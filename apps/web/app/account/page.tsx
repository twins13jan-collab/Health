"use client";

import { useEffect, useState, type FormEvent } from "react";
import { PageHead, Skeleton } from "@/components/ui";
import { api, getSessionUser, type SessionUser } from "@/lib/api";
import { useLang } from "@/lib/i18n";

/** My account: real session profile from /api/auth/me + password change. */
export default function AccountPage() {
  const { lang } = useLang();
  const ar = lang === "ar";
  const [user, setUser] = useState<SessionUser | null>(() => getSessionUser());
  const [form, setForm] = useState({ current: "", next: "", confirm: "" });
  const [msg, setMsg] = useState<{ ok: boolean; text: string } | null>(null);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    void api.me().then(setUser).catch(() => { /* session guard handles 401 */ });
  }, []);

  const onChange = async (e: FormEvent) => {
    e.preventDefault();
    setMsg(null);
    if (form.next !== form.confirm) {
      setMsg({ ok: false, text: ar ? "تأكيد كلمة المرور غير مطابق" : "Password confirmation does not match" });
      return;
    }
    setBusy(true);
    try {
      await api.changePassword(form.current, form.next);
      setMsg({ ok: true, text: ar ? "تم تغيير كلمة المرور بنجاح" : "Password changed successfully" });
      setForm({ current: "", next: "", confirm: "" });
    } catch (err) {
      setMsg({ ok: false, text: err instanceof Error ? err.message : String(err) });
    } finally {
      setBusy(false);
    }
  };

  return (
    <>
      <PageHead
        eyebrow={ar ? "الحساب" : "Account"}
        title={ar ? "حسابي" : "My Account"}
        sub={ar ? "بيانات الجلسة الحالية وإدارة كلمة المرور" : "Your current session profile and password management"}
      />

      <section className="card">
        <h2>{ar ? "الملف الشخصي" : "Profile"}</h2>
        {user ? (
          <div className="tbl-wrap plain">
            <table className="tbl">
              <tbody>
                <tr><th>{ar ? "الاسم" : "Name"}</th><td>{user.fullName || "—"}</td></tr>
                <tr><th>{ar ? "البريد الإلكتروني" : "Email"}</th><td dir="ltr">{user.email}</td></tr>
                <tr><th>{ar ? "الدور" : "Role"}</th><td dir="ltr">{user.role}</td></tr>
                <tr><th>{ar ? "المحافظة" : "Governorate"}</th><td>{user.governorate || "—"}</td></tr>
                <tr><th>{ar ? "المديرية" : "Directorate"}</th><td>{user.directorate || "—"}</td></tr>
                <tr><th>{ar ? "معرّف المنشأة" : "Facility ID"}</th><td dir="ltr">{user.facilityId || "—"}</td></tr>
              </tbody>
            </table>
          </div>
        ) : (
          <Skeleton rows={3} />
        )}
      </section>

      <section className="card">
        <h2>{ar ? "تغيير كلمة المرور" : "Change password"}</h2>
        <form onSubmit={onChange}>
          <div className="form-grid">
            <div className="field">
              <label>{ar ? "كلمة المرور الحالية" : "Current password"}</label>
              <input type="password" required dir="ltr" autoComplete="current-password" value={form.current} onChange={(e) => setForm({ ...form, current: e.target.value })} />
            </div>
            <div className="field">
              <label>{ar ? "كلمة المرور الجديدة" : "New password"}</label>
              <input type="password" required dir="ltr" autoComplete="new-password" value={form.next} onChange={(e) => setForm({ ...form, next: e.target.value })} />
            </div>
            <div className="field">
              <label>{ar ? "تأكيد كلمة المرور الجديدة" : "Confirm new password"}</label>
              <input type="password" required dir="ltr" autoComplete="new-password" value={form.confirm} onChange={(e) => setForm({ ...form, confirm: e.target.value })} />
            </div>
          </div>
          <div className="form-actions">
            <button className="btn" type="submit" disabled={busy}>{busy ? "…" : ar ? "تغيير كلمة المرور" : "Change password"}</button>
          </div>
        </form>
        <p className="foot-note">{ar ? "السياسة: 10 أحرف على الأقل وتشمل حروفًا وأرقامًا." : "Policy: at least 10 characters including letters and digits."}</p>
        {msg ? <p className={`form-msg ${msg.ok ? "ok" : "err"}`}>{msg.text}</p> : null}
      </section>
    </>
  );
}
