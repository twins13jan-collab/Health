"use client";

import { useCallback, useEffect, useState } from "react";
import { Button, PageHead, Toasts } from "@/components/ui";
import { api } from "@/lib/api";
import { useLang } from "@/lib/i18n";

interface PendingRow { user: { id: string; email: string; full_name?: string; role: string; governorate?: string; directorate?: string }; facility_name?: string }

/** Staff-account approvals: people who registered themselves under
 *  a facility (role → entity → governorate → directorate →
 *  facility) wait here until the facility manager approves them.
 *  Approval activates the account with exactly the role and the
 *  facility scope chosen at registration. */
export default function FacilityStaffPage() {
  const { lang, t } = useLang();
  const ar = lang === "ar";
  const [rows, setRows] = useState<PendingRow[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [toasts, setToasts] = useState<{ text: string; err?: boolean }[]>([]);
  const [busy, setBusy] = useState(false);

  const toast = (text: string, err?: boolean) => { setToasts((x) => [...x, { text, err }]); setTimeout(() => setToasts((x) => x.slice(1)), 4500); };

  const load = useCallback(async () => {
    try {
      const r = await api.getJson<{ pending: PendingRow[] }>("/api/facility-staff/pending");
      setRows(r.pending ?? []);
    } catch (e) { setError(e instanceof Error ? e.message : String(e)); }
  }, []);
  useEffect(() => { load(); }, [load]);

  async function act(id: string, action: "approve" | "reject", okMsg: string) {
    setBusy(true);
    try { await api.postJson(`/api/facility-staff/${id}/${action}`, {}); toast(okMsg); await load(); }
    catch (e) { toast(e instanceof Error ? e.message : String(e), true); }
    finally { setBusy(false); }
  }

  return (
    <div>
      <PageHead eyebrow={ar ? "إدارة المنشأة" : "Facility administration"} title={t("staffApprovals")}
        sub={ar ? "حسابات الطاقم التي سجّلت نفسها تحت منشأتك بالتسلسل الكامل (الدور ← الجهة ← المحافظة ← المديرية ← المنشأة). الاعتماد يفعّل الحساب بنفس الدور والنطاق المختارين عند التسجيل."
               : "Staff accounts that registered under your facility through the full hierarchy (role → entity → governorate → directorate → facility). Approval activates the account with exactly that role and scope."} />
      {error ? <div className="alert-error">{error}</div> : null}
      {rows.length === 0 ? <div className="empty">{ar ? "لا طلبات اعتماد معلّقة" : "No pending approvals"}</div> : (
        <div className="cards-grid">
          {rows.map((r) => (
            <section key={r.user.id} className="card">
              <div className="l-row"><strong>{r.user.full_name || r.user.email}</strong><span className="badge warn">{r.user.role}</span></div>
              <div className="l-row">
                <span className="badge info">{r.user.email}</span>
                {r.facility_name ? <span className="badge info">{r.facility_name}</span> : null}
                {r.user.governorate ? <span className="badge info">{r.user.governorate}</span> : null}
              </div>
              <div className="l-actions">
                <Button disabled={busy} onClick={() => act(r.user.id, "approve", ar ? "تم اعتماد الحساب وتفعيله" : "Account approved & activated")}>{ar ? "اعتماد وتفعيل" : "Approve & activate"}</Button>
                <Button disabled={busy} onClick={() => act(r.user.id, "reject", ar ? "تم رفض الطلب وحذفه" : "Request rejected")}>{ar ? "رفض" : "Reject"}</Button>
              </div>
            </section>
          ))}
        </div>
      )}
      <Toasts items={toasts} />
    </div>
  );
}
