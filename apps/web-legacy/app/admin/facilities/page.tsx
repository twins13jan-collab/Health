"use client";

import { useCallback, useEffect, useState } from "react";
import { api } from "@/lib/api";
import { useLang } from "@/lib/i18n";

const TYPE_AR: Record<string, string> = { hospital: "مستشفى", health_unit: "وحدة صحية", family_center: "مركز صحة أسرة", lab: "معمل", pharmacy: "صيدلية", blood_bank: "بنك دم", dialysis_center: "مركز غسيل كلوي", oncology_center: "مركز أورام", clinic: "عيادة", center: "مركز طبي" };
interface Facility { id: string; nameAr: string; type: string; governorate: string; city?: string; status: string; bedCapacity?: number; createdAt: string }

/** Admin: facilities in every state. Self-registered facilities
 *  wait as 'pending' — approving activates the facility and the
 *  manager account created with its registration. */
export default function AdminFacilitiesPage() {
  const { lang } = useLang();
  const ar = lang === "ar";
  const [rows, setRows] = useState<Facility[]>([]);
  const [filter, setFilter] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [toast, setToast] = useState<string | null>(null);

  const load = useCallback(async () => {
    try {
      const r = await api.getJson<{ facilities: Facility[] }>(`/api/admin/facilities${filter ? `?status=${filter}` : ""}`);
      setRows(r.facilities);
    } catch (e) { setError(e instanceof Error ? e.message : String(e)); }
  }, [filter]);
  useEffect(() => { load(); }, [load]);

  async function act(id: string, action: "approve" | string) {
    try {
      if (action === "approve") await api.postJson(`/api/admin/facilities/${id}/approve`, {});
      else await api.postJson(`/api/admin/facilities/${id}/status`, { status: action });
      setToast(ar ? "تم" : "Done"); setTimeout(() => setToast(null), 3000);
      await load();
    } catch (e) { setError(e instanceof Error ? e.message : String(e)); }
  }

  const badge = (s: string) => s === "active" ? <span className="badge ok">{ar ? "مفعّلة" : "Active"}</span>
    : s === "pending" ? <span className="badge warn">{ar ? "بانتظار الاعتماد" : "Pending"}</span>
    : <span className="badge danger">{ar ? "موقوفة" : "Suspended"}</span>;

  return (
    <div>
      <div className="page-head">
        <h1>🏥 {ar ? "إدارة المنشآت والتسجيلات" : "Facilities & Registrations"}</h1>
        <p>{ar ? "كل المنشآت بكل حالاتها. المنشأة المسجلة ذاتيًا لا تعمل ولا يفعَّل حساب مديرها إلا باعتمادك هنا." : "Every facility in every state. A self-registered facility — and its manager account — stays inactive until you approve it here."}</p>
      </div>
      {error && <div className="error-banner">{error}</div>}
      {toast && <div className="toast">{toast}</div>}
      <div className="card">
        <div className="seg-row">
          {[["", ar ? "الكل" : "All"], ["pending", ar ? "بانتظار الاعتماد" : "Pending"], ["active", ar ? "مفعّلة" : "Active"], ["suspended", ar ? "موقوفة" : "Suspended"]].map(([v, label]) => (
            <button key={v} type="button" className={`btn btn-small ${filter === v ? "" : "btn-secondary"}`} onClick={() => setFilter(v)}>{label}</button>
          ))}
        </div>
        <table className="table">
          <thead><tr><th>{ar ? "المنشأة" : "Facility"}</th><th>{ar ? "النوع" : "Type"}</th><th>{ar ? "المحافظة" : "Governorate"}</th><th>{ar ? "الحالة" : "Status"}</th><th></th></tr></thead>
          <tbody>
            {rows.map((f) => (
              <tr key={f.id}>
                <td><strong>{f.nameAr}</strong>{f.city ? <div className="muted" style={{ fontSize: 12 }}>{f.city}</div> : null}</td>
                <td>{ar ? (TYPE_AR[f.type] ?? f.type) : f.type}</td><td>{f.governorate}</td><td>{badge(f.status)}</td>
                <td>
                  <span className="l-actions">
                    {f.status === "pending" && <button type="button" className="btn btn-small" onClick={() => act(f.id, "approve")}>{ar ? "اعتماد وتفعيل" : "Approve & activate"}</button>}
                    {f.status === "active" && <button type="button" className="btn btn-small btn-secondary" onClick={() => act(f.id, "suspended")}>{ar ? "إيقاف" : "Suspend"}</button>}
                    {f.status === "suspended" && <button type="button" className="btn btn-small" onClick={() => act(f.id, "active")}>{ar ? "إعادة تفعيل" : "Reactivate"}</button>}
                  </span>
                </td>
              </tr>
            ))}
            {rows.length === 0 && <tr><td colSpan={5} className="muted">{ar ? "لا منشآت في هذه الحالة." : "No facilities in this state."}</td></tr>}
          </tbody>
        </table>
      </div>
    </div>
  );
}
