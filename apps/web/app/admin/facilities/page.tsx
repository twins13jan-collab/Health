"use client";

import { useCallback, useEffect, useState } from "react";
import { PageHead, SegChips, Toasts } from "@/components/ui";
import { api } from "@/lib/api";
import { useLang } from "@/lib/i18n";
import { facilityTypeLabel } from "@/lib/types";

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
    <>
      <PageHead
        eyebrow={ar ? "الإدارة" : "Administration"}
        title={`🏥 ${ar ? "إدارة المنشآت والتسجيلات" : "Facilities & Registrations"}`}
        sub={ar ? "كل المنشآت بكل حالاتها. المنشأة المسجلة ذاتيًا لا تعمل ولا يفعَّل حساب مديرها إلا باعتمادك هنا." : "Every facility in every state. A self-registered facility — and its manager account — stays inactive until you approve it here."}
      />
      {error && <div className="alert-error">{error}</div>}
      <Toasts items={toast ? [{ text: toast }] : []} />
      <div className="card">
        <SegChips
          value={filter}
          onChange={setFilter}
          options={[
            { value: "", label: ar ? "الكل" : "All" },
            { value: "pending", label: ar ? "بانتظار الاعتماد" : "Pending" },
            { value: "active", label: ar ? "مفعّلة" : "Active" },
            { value: "suspended", label: ar ? "موقوفة" : "Suspended" },
          ]}
        />
        <div className="tbl-wrap">
          <table className="tbl">
            <thead><tr><th>{ar ? "المنشأة" : "Facility"}</th><th>{ar ? "النوع" : "Type"}</th><th>{ar ? "المحافظة" : "Governorate"}</th><th>{ar ? "الحالة" : "Status"}</th><th></th></tr></thead>
            <tbody>
              {rows.map((f) => (
                <tr key={f.id}>
                  <td><strong>{f.nameAr}</strong>{f.city ? <div className="muted small">{f.city}</div> : null}</td>
                  <td>{facilityTypeLabel(f.type, lang)}</td><td>{f.governorate}</td><td>{badge(f.status)}</td>
                  <td>
                    <span className="row">
                      {f.status === "pending" && <button type="button" className="btn btn-sm" onClick={() => act(f.id, "approve")}>{ar ? "اعتماد وتفعيل" : "Approve & activate"}</button>}
                      {f.status === "active" && <button type="button" className="btn btn-soft btn-sm" onClick={() => act(f.id, "suspended")}>{ar ? "إيقاف" : "Suspend"}</button>}
                      {f.status === "suspended" && <button type="button" className="btn btn-sm" onClick={() => act(f.id, "active")}>{ar ? "إعادة تفعيل" : "Reactivate"}</button>}
                    </span>
                  </td>
                </tr>
              ))}
              {rows.length === 0 && <tr><td colSpan={5} className="muted">{ar ? "لا منشآت في هذه الحالة." : "No facilities in this state."}</td></tr>}
            </tbody>
          </table>
        </div>
      </div>
    </>
  );
}
