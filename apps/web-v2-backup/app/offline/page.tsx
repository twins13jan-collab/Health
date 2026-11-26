"use client";
import { useEffect, useState } from "react";
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
    <div className="content-narrow-s">
      <h1>العمل دون اتصال والمزامنة</h1>
      <p>هذا المتصفح جهاز مسجل يعمل داخل حزمة محددة النطاق والمدة. الكتابات أثناء انقطاع الشبكة تُحفظ محليًا بمعرفات ثابتة وتُزامَن في مظروف واحد لا يتكرر تطبيقه، والتعارضات السريرية أو المالية تصبح حالات عند المدير — لا تُحسم بصمت.</p>
      <p>عمليات في الانتظار: <strong>{pending}</strong></p>
      <button onClick={sync} disabled={busy} className="btn">{busy ? "جارٍ المزامنة..." : "مزامنة الآن"}</button>
      {message && <p>{message}</p>}
      <h2 className="section-title">تعارضات مفتوحة عند المدير</h2>
      {conflicts.length === 0 ? <p>لا تعارضات مفتوحة</p> : (
        <ul>{conflicts.map((c) => <li key={c.id}>{c.id} — {c.domain ?? ""} — {c.status ?? ""}</li>)}</ul>
      )}
    </div>
  );
}
