"use client";

import { useCallback, useEffect, useState, type FormEvent } from "react";
import { adminUsers, getSessionUser, type AdminUser } from "@/lib/api";
import { useLang } from "@/lib/i18n";

const ROLES = [
  "admin", "national_admin", "governorate_admin", "directorate_admin", "facility_manager",
  "physician", "nurse", "receptionist", "pharmacist", "finance_manager", "store_keeper",
  "biomedical_engineer", "oncologist", "ipc_officer", "safety_officer", "social_worker",
  "health_educator", "school_counselor", "forensic_physician",
];

export default function AdminUsersPage() {
  const { lang } = useLang();
  const ar = lang === "ar";
  const [me] = useState(() => getSessionUser());
  const isAdmin = me?.role === "admin" || me?.role === "national_admin";
  const [rows, setRows] = useState<AdminUser[]>([]);
  const [total, setTotal] = useState(0);
  const [q, setQ] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [notice, setNotice] = useState<string | null>(null);
  const [form, setForm] = useState({ email: "", password: "", fullName: "", role: "receptionist", facilityId: "", governorate: "", directorate: "" });
  const [busy, setBusy] = useState(false);

  const load = useCallback(async (query = "") => {
    try {
      const r = await adminUsers.list(query, 0, 50);
      setRows(r.items);
      setTotal(r.total);
      setError(null);
    } catch (e) {
      setError(e instanceof Error ? e.message : String(e));
    }
  }, []);

  useEffect(() => { if (isAdmin) void load(); }, [isAdmin, load]);

  if (!isAdmin) {
    return (
      <>
        <div className="page-head"><h1>{ar ? "إدارة الحسابات والمستخدمين" : "Accounts & Users"}</h1></div>
        <div className="error-banner">{ar ? "هذه الصفحة مقصورة على مسؤولي النظام" : "This page is restricted to system administrators"}</div>
      </>
    );
  }

  const onCreate = async (e: FormEvent) => {
    e.preventDefault();
    setBusy(true); setError(null); setNotice(null);
    try {
      await adminUsers.create({
        email: form.email.trim(),
        password: form.password,
        role: form.role,
        fullName: form.fullName || undefined,
        facilityId: form.facilityId || undefined,
        governorate: form.governorate || undefined,
        directorate: form.directorate || undefined,
      });
      setNotice(ar ? "تم إنشاء الحساب بنجاح" : "Account created");
      setForm({ email: "", password: "", fullName: "", role: "receptionist", facilityId: "", governorate: "", directorate: "" });
      void load(q);
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    } finally { setBusy(false); }
  };

  const toggleActive = async (u: AdminUser) => {
    setError(null); setNotice(null);
    try {
      if (u.is_active) await adminUsers.deactivate(u.id);
      else await adminUsers.activate(u.id);
      void load(q);
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    }
  };

  const resetPassword = async (u: AdminUser) => {
    const pw = window.prompt(ar ? `كلمة مرور جديدة لحساب ${u.email} (10 أحرف على الأقل، حروف وأرقام):` : `New password for ${u.email} (min 10 chars, letters and digits):`);
    if (!pw) return;
    setError(null); setNotice(null);
    try {
      await adminUsers.resetPassword(u.id, pw);
      setNotice(ar ? "تمت إعادة تعيين كلمة المرور وفكّ أي قفل على الحساب" : "Password reset and any lock cleared");
    } catch (err) {
      setError(err instanceof Error ? err.message : String(err));
    }
  };

  return (
    <>
      <div className="page-head">
        <h1>{ar ? "إدارة الحسابات والمستخدمين" : "Accounts & Users"}</h1>
        <p>{ar ? "إنشاء حسابات العاملين وتعيين الأدوار والمنشآت، والتفعيل والتعطيل وإعادة تعيين كلمات المرور — كل إجراء موثّق في سجل التدقيق" : "Provision staff accounts with roles and facilities; activation, deactivation and password resets — every action is audit-logged"}</p>
      </div>
      {error ? <div className="error-banner" role="alert">{error}</div> : null}
      {notice ? <p className="notice-ok">{notice}</p> : null}

      <section className="card">
        <h2>{ar ? "إنشاء حساب جديد" : "Create account"}</h2>
        <form onSubmit={onCreate} className="form-grid">
          <div className="field"><label>{ar ? "البريد الإلكتروني *" : "Email *"}</label>
            <input type="email" required dir="ltr" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} /></div>
          <div className="field"><label>{ar ? "كلمة المرور المؤقتة *" : "Temporary password *"}</label>
            <input type="text" required dir="ltr" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} placeholder={ar ? "10+ أحرف، حروف وأرقام" : "10+ chars, letters & digits"} /></div>
          <div className="field"><label>{ar ? "الاسم الكامل" : "Full name"}</label>
            <input value={form.fullName} onChange={(e) => setForm({ ...form, fullName: e.target.value })} /></div>
          <div className="field"><label>{ar ? "الدور *" : "Role *"}</label>
            <select value={form.role} onChange={(e) => setForm({ ...form, role: e.target.value })}>
              {ROLES.map((r) => <option key={r} value={r}>{r}</option>)}
            </select></div>
          <div className="field"><label>{ar ? "معرّف المنشأة (UUID)" : "Facility ID (UUID)"}</label>
            <input dir="ltr" value={form.facilityId} onChange={(e) => setForm({ ...form, facilityId: e.target.value })} /></div>
          <div className="field"><label>{ar ? "المحافظة" : "Governorate"}</label>
            <input value={form.governorate} onChange={(e) => setForm({ ...form, governorate: e.target.value })} /></div>
          <div className="field"><label>{ar ? "المديرية" : "Directorate"}</label>
            <input value={form.directorate} onChange={(e) => setForm({ ...form, directorate: e.target.value })} /></div>
          <button className="btn" type="submit" disabled={busy}>{busy ? "…" : ar ? "إنشاء الحساب" : "Create account"}</button>
        </form>
      </section>

      <section className="card">
        <h2>{ar ? `الحسابات (${total})` : `Accounts (${total})`}</h2>
        <form onSubmit={(e) => { e.preventDefault(); void load(q); }} className="l-mb">
          <input placeholder={ar ? "بحث بالبريد أو الاسم…" : "Search by email or name…"} value={q} onChange={(e) => setQ(e.target.value)} className="w-select-lg" />
          <button type="submit" className="ms-8">{ar ? "بحث" : "Search"}</button>
        </form>
        <div className="table-wrap">
          <table className="table">
            <thead><tr>
              <th>{ar ? "البريد" : "Email"}</th><th>{ar ? "الاسم" : "Name"}</th><th>{ar ? "الدور" : "Role"}</th>
              <th>{ar ? "المحافظة" : "Governorate"}</th><th>{ar ? "الحالة" : "Status"}</th><th>{ar ? "آخر دخول" : "Last login"}</th><th>{ar ? "إجراءات" : "Actions"}</th>
            </tr></thead>
            <tbody>
              {rows.map((u) => (
                <tr key={u.id}>
                  <td dir="ltr">{u.email}</td>
                  <td>{u.full_name}</td>
                  <td dir="ltr">{u.role}</td>
                  <td>{u.governorate}</td>
                  <td>{u.is_active ? (ar ? "مفعّل" : "Active") : (ar ? "معطّل" : "Disabled")}</td>
                  <td dir="ltr">{u.last_login_at ? u.last_login_at.slice(0, 16).replace("T", " ") : "—"}</td>
                  <td>
                    <button type="button" className="btn btn-ghost" onClick={() => void toggleActive(u)}>
                      {u.is_active ? (ar ? "تعطيل" : "Deactivate") : (ar ? "تفعيل" : "Activate")}
                    </button>{" "}
                    <button type="button" className="btn btn-ghost" onClick={() => void resetPassword(u)}>
                      {ar ? "إعادة تعيين كلمة المرور" : "Reset password"}
                    </button>
                  </td>
                </tr>
              ))}
              {rows.length === 0 ? <tr><td colSpan={7}>{ar ? "لا توجد حسابات مطابقة" : "No matching accounts"}</td></tr> : null}
            </tbody>
          </table>
        </div>
      </section>
    </>
  );
}
