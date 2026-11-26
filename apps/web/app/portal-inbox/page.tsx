"use client";

import { useCallback, useEffect, useState } from "react";
import { PageHead, SegChips, Toasts } from "@/components/ui";
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

  const typeLabel = (ty: string) => ({ lab: ar ? "تحليل معملي" : "Lab test", pharmacy: ar ? "صيدلية" : "Pharmacy", radiology: ar ? "أشعة وتصوير طبي" : "Radiology & imaging", dialysis: ar ? "غسيل كلوي" : "Dialysis", home_visit: ar ? "زيارة منزلية" : "Home visit", other: ar ? "أخرى" : "Other" } as Record<string, string>)[ty] ?? ty;
  const statusBadge = (s: string) =>
    s === "done" ? <span className="badge ok">{ar ? "منجزة" : "Done"}</span>
    : s === "confirmed" ? <span className="badge info">{ar ? "مؤكدة" : "Confirmed"}</span>
    : s === "cancelled" ? <span className="badge danger">{ar ? "ملغاة" : "Cancelled"}</span>
    : <span className="badge warn">{ar ? "جديدة" : "New"}</span>;

  return (
    <>
      <PageHead
        eyebrow={ar ? "بوابة المرضى" : "Patient portal"}
        title={ar ? "طلبات خدمات البوابة" : "Portal Service Requests"}
        sub={ar ? "طلبات المرضى الواردة من البوابة العامة لمنشأتك: أكّدها أو أنجزها أو ألغها بسبب يظهر للمريض." : "Patient requests arriving from the public portal to your facility: confirm, complete, or cancel with a note the patient sees."}
      />
      {error && <div className="alert-error">{error}</div>}
      <Toasts items={toast ? [{ text: toast }] : []} />
      <div className="card">
        <SegChips
          value={filter}
          onChange={setFilter}
          options={[
            { value: "requested", label: ar ? "جديدة" : "New" },
            { value: "confirmed", label: ar ? "مؤكدة" : "Confirmed" },
            { value: "done", label: ar ? "منجزة" : "Done" },
            { value: "", label: ar ? "الكل" : "All" },
          ]}
        />
        <div className="tbl-wrap">
          <table className="tbl">
            <thead><tr><th>{ar ? "المريض" : "Patient"}</th><th>{ar ? "الخدمة" : "Service"}</th><th>{ar ? "التفاصيل" : "Details"}</th><th>{ar ? "التاريخ المفضل" : "Preferred"}</th><th>{ar ? "الحالة" : "Status"}</th><th></th></tr></thead>
            <tbody>
              {rows.map((r) => (
                <tr key={r.request.id}>
                  <td>{r.patientName}</td>
                  <td>{typeLabel(r.request.serviceType)}</td>
                  <td>{r.request.details ?? "—"}</td>
                  <td>{r.request.preferredDate ?? "—"}</td>
                  <td>{statusBadge(r.request.status)}</td>
                  <td>
                    <span className="row">
                      {r.request.status === "requested" && <button type="button" className="btn btn-sm" onClick={() => setStatus(r.request.id, "confirmed")}>{ar ? "تأكيد" : "Confirm"}</button>}
                      {r.request.status === "confirmed" && <button type="button" className="btn btn-sm" onClick={() => setStatus(r.request.id, "done")}>{ar ? "إنجاز" : "Complete"}</button>}
                      {(r.request.status === "requested" || r.request.status === "confirmed") && <button type="button" className="btn btn-soft btn-sm" onClick={() => setStatus(r.request.id, "cancelled")}>{ar ? "إلغاء" : "Cancel"}</button>}
                    </span>
                  </td>
                </tr>
              ))}
              {rows.length === 0 && <tr><td colSpan={6} className="muted">{ar ? "لا طلبات في هذه الحالة." : "No requests in this state."}</td></tr>}
            </tbody>
          </table>
        </div>
      </div>
    </>
  );
}
