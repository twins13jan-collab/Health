"use client";

import { useCallback, useEffect, useState } from "react";
import { api } from "@/lib/api";
import { useLang } from "@/lib/i18n";

interface ReqRow { request: { id: string; serviceType: string; details?: string; preferredDate?: string; status: string; createdAt: string }; patientName: string }

/** Facility inbox for patient-portal service requests: confirm,
 *  complete, or cancel (with a note the patient sees). */
export default function PortalInboxPage() {
  const { lang } = useLang();
  const ar = lang === "ar";
  const [rows, setRows] = useState<ReqRow[]>([]);
  const [filter, setFilter] = useState("requested");
  const [error, setError] = useState<string | null>(null);
  const [toast, setToast] = useState<string | null>(null);

  const load = useCallback(async () => {
    try {
      const r = await api.getJson<{ requests: ReqRow[] }>(`/api/portal/inbox${filter ? `?status=${filter}` : ""}`);
      setRows(r.requests);
    } catch (e) { setError(e instanceof Error ? e.message : String(e)); }
  }, [filter]);
  useEffect(() => { load(); }, [load]);

  async function setStatus(id: string, status: string) {
    const note: string = status === "cancelled" ? window.prompt(ar ? "سبب الإلغاء (يظهر للمريض):" : "Cancellation reason (patient sees it):") ?? "" : "";
    if (status === "cancelled" && !note.trim()) return;
    try {
      await api.postJson(`/api/portal/inbox/${id}/status`, { status, note: note || undefined });
      setToast(ar ? "حُدّث الطلب" : "Request updated"); setTimeout(() => setToast(null), 3000);
      await load();
    } catch (e) { setError(e instanceof Error ? e.message : String(e)); }
  }

  const typeLabel = (ty: string) => ({ lab: ar ? "تحليل معملي" : "Lab test", pharmacy: ar ? "صيدلية" : "Pharmacy", home_visit: ar ? "زيارة منزلية" : "Home visit", other: ar ? "أخرى" : "Other" } as Record<string, string>)[ty] ?? ty;

  return (
    <div>
      <div className="page-head">
        <h1>{ar ? "طلبات خدمات البوابة" : "Portal Service Requests"}</h1>
        <p>{ar ? "طلبات المرضى الواردة من البوابة العامة لمنشأتك: أكّدها أو أنجزها أو ألغها بسبب يظهر للمريض." : "Patient requests arriving from the public portal to your facility: confirm, complete, or cancel with a note the patient sees."}</p>
      </div>
      {error && <div className="error-banner">{error}</div>}
      {toast && <div className="toast">{toast}</div>}
      <div className="card">
        <div className="seg-row">
          {[["requested", ar ? "جديدة" : "New"], ["confirmed", ar ? "مؤكدة" : "Confirmed"], ["done", ar ? "منجزة" : "Done"], ["", ar ? "الكل" : "All"]].map(([v, label]) => (
            <button key={v} type="button" className={`btn btn-small ${filter === v ? "" : "btn-secondary"}`} onClick={() => setFilter(v)}>{label}</button>
          ))}
        </div>
        <table className="table">
          <thead><tr><th>{ar ? "المريض" : "Patient"}</th><th>{ar ? "الخدمة" : "Service"}</th><th>{ar ? "التفاصيل" : "Details"}</th><th>{ar ? "التاريخ المفضل" : "Preferred"}</th><th>{ar ? "الحالة" : "Status"}</th><th></th></tr></thead>
          <tbody>
            {rows.map((r) => (
              <tr key={r.request.id}>
                <td>{r.patientName}</td>
                <td>{typeLabel(r.request.serviceType)}</td>
                <td>{r.request.details ?? "—"}</td>
                <td>{r.request.preferredDate ?? "—"}</td>
                <td>{r.request.status}</td>
                <td>
                  <span className="l-actions">
                    {r.request.status === "requested" && <button type="button" className="btn btn-small" onClick={() => setStatus(r.request.id, "confirmed")}>{ar ? "تأكيد" : "Confirm"}</button>}
                    {r.request.status === "confirmed" && <button type="button" className="btn btn-small" onClick={() => setStatus(r.request.id, "done")}>{ar ? "إنجاز" : "Complete"}</button>}
                    {(r.request.status === "requested" || r.request.status === "confirmed") && <button type="button" className="btn btn-small btn-secondary" onClick={() => setStatus(r.request.id, "cancelled")}>{ar ? "إلغاء" : "Cancel"}</button>}
                  </span>
                </td>
              </tr>
            ))}
            {rows.length === 0 && <tr><td colSpan={6} className="muted">{ar ? "لا طلبات في هذه الحالة." : "No requests in this state."}</td></tr>}
          </tbody>
        </table>
      </div>
    </div>
  );
}
