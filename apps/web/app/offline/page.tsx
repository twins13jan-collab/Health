"use client";
import { useEffect, useState } from "react";
import { PageHead } from "@/components/ui";
import { api } from "@/lib/api";
import { pendingOffline, syncOfflineQueue, type SyncResult } from "@/lib/offline";

interface Conflict { id: string; domain?: string; status?: string }

export default function OfflinePage() {
  const [pending, setPending] = useState(0);
  const [conflicts, setConflicts] = useState<Conflict[]>([]);
  const [message, setMessage] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  async function refresh() {
    setPending(pendingOffline().length);
    try {
      const res = await api.moduleList<Conflict>("/api/offline/conflicts/open", "conflicts");
      setConflicts(res.data);
    } catch { /* conflicts view is best-effort */ }
  }
  useEffect(() => { refresh(); }, []);

  async function sync() {
    setBusy(true); setMessage(null);
    try {
      const { result, sent } = await syncOfflineQueue();
      const r = result as SyncResult;
      setMessage(sent === 0 ? "لا شيء في الطابور" : `زومِن ${sent} عملية: مطبقة ${r.applied ?? 0}، متعارضة ${r.conflicted ?? 0}، مكررة ${r.duplicates ?? 0}`);
      await refresh();
    } catch (e) {
      setMessage(`فشلت المزامنة: ${e instanceof Error ? e.message : e} — بقي الطابور كما هو`);
    } finally { setBusy(false); }
  }

  return (
    <>
      <PageHead
        eyebrow="دورة 237"
        title="العمل دون اتصال والمزامنة"
        sub="هذا المتصفح جهاز مسجل يعمل داخل حزمة محددة النطاق والمدة. الكتابات أثناء انقطاع الشبكة تُحفظ محليًا بمعرفات ثابتة وتُزامَن في مظروف واحد لا يتكرر تطبيقه، والتعارضات السريرية أو المالية تصبح حالات عند المدير — لا تُحسم بصمت."
      />
      <section className="card">
        <div className="kpi-grid flush">
          <div className="kpi">
            <div className="kpi-label">عمليات في الانتظار</div>
            <div className="kpi-value">{pending}</div>
          </div>
          <div className="kpi">
            <div className="kpi-label">تعارضات مفتوحة عند المدير</div>
            <div className="kpi-value">{conflicts.length}</div>
          </div>
        </div>
        <div className="form-actions">
          <button onClick={sync} disabled={busy} className="btn">{busy ? "جارٍ المزامنة..." : "مزامنة الآن"}</button>
        </div>
        {message && <p className="form-msg">{message}</p>}
      </section>
      <section className="card">
        <h2>تعارضات مفتوحة عند المدير</h2>
        {conflicts.length === 0 ? <div className="empty">لا تعارضات مفتوحة</div> : (
          <div className="tbl-wrap">
            <table className="tbl">
              <thead><tr><th>المعرّف</th><th>المجال</th><th>الحالة</th></tr></thead>
              <tbody>
                {conflicts.map((c) => <tr key={c.id}><td dir="ltr">{c.id}</td><td>{c.domain ?? ""}</td><td>{c.status ?? ""}</td></tr>)}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </>
  );
}
