"use client";

import { useCallback, useEffect, useState } from "react";
import { api } from "@/lib/api";
import { useLang } from "@/lib/i18n";
import { Button, Card, Drawer, EmptyState, ErrorBanner, PageHead, Toasts } from "@/components/ui";
import KpiCard from "@/components/KpiCard";

type InvRow = {
  id: string; patient_id: string; encounter_id: string | null; payer: string | null; status: string;
  total: number; insurer_amount: number; patient_amount: number; age_days: number; bucket: string; rank: number;
};
type Board = { invoices: InvRow[]; summary: Record<string, unknown> };

const STATUS_AR: Record<string, string> = { draft: "مسودة", issued: "مُصدرة", claimed: "مُطالب بها", partially_paid: "مدفوعة جزئيًا", paid: "مدفوعة", rejected: "مرفوضة", cancelled: "ملغاة" };
const BUCKET_AR: Record<string, string> = { current: "حتى 30 يومًا", d31_60: "31–60 يومًا", d61_90: "61–90 يومًا", over_90: "أكثر من 90 يومًا" };
const NEXT: Record<string, string[]> = { draft: ["issued", "cancelled"], issued: ["claimed", "paid", "cancelled"], claimed: ["partially_paid", "paid", "rejected"], partially_paid: ["paid"], rejected: ["issued"] };

export default function FinanceBoardPage() {
  const { lang } = useLang();
  const ar = lang === "ar";
  const [board, setBoard] = useState<Board | null>(null);
  const [err, setErr] = useState("");
  const [toasts, setToasts] = useState<{ text: string; err?: boolean }[]>([]);
  const [lineInv, setLineInv] = useState<InvRow | null>(null);
  const [newInv, setNewInv] = useState(false);
  const [invForm, setInvForm] = useState({ patientId: "", payer: "" });
  const [lineForm, setLineForm] = useState({ description: "", unitPrice: "", quantity: "1", coveragePct: "80" });

  const toast = (text: string, isErr = false) => setToasts((t) => [...t, { text, err: isErr }]);
  const load = useCallback(async () => {
    try { setBoard(await api.getJson<Board>("/api/billing/board")); setErr(""); }
    catch (e) { setErr(e instanceof Error ? e.message : String(e)); }
  }, []);
  useEffect(() => {
    load();
    const id = window.setInterval(load, 60000);
    return () => window.clearInterval(id);
  }, [load]);

  const move = async (inv: InvRow, to: string) => {
    try {
      await api.postJson(`/api/billing/invoices/${inv.id}/status`, { status: to });
      toast(`${ar ? "صارت الفاتورة" : "Invoice now"}: ${STATUS_AR[to] ?? to}`);
      load();
    } catch (e) { toast(e instanceof Error ? e.message : String(e), true); }
  };
  const createInv = async () => {
    try {
      await api.postJson("/api/billing/invoices", { patientId: invForm.patientId, payer: invForm.payer || null });
      toast(ar ? "أُنشئت الفاتورة كمسودة" : "Invoice created as draft");
      setNewInv(false); load();
    } catch (e) { toast(e instanceof Error ? e.message : String(e), true); }
  };
  const addLine = async () => {
    if (!lineInv) return;
    try {
      await api.postJson(`/api/billing/invoices/${lineInv.id}/lines`, {
        description: lineForm.description, unitPrice: Number(lineForm.unitPrice),
        quantity: Number(lineForm.quantity), coveragePct: Number(lineForm.coveragePct),
      });
      toast(ar ? "أُضيف البند وأُعيد حساب الإجمالي وحصص التغطية خادميًا" : "Line added; totals and coverage shares recomputed server-side");
      setLineInv(null); load();
    } catch (e) { toast(e instanceof Error ? e.message : String(e), true); }
  };

  const s = (board?.summary ?? {}) as Record<string, number>;
  const buckets = ((board?.summary as { aging_buckets?: Record<string, number> } | undefined)?.aging_buckets) ?? {};
  const money = (v: number | undefined) => (v == null ? "—" : Number(v).toLocaleString(ar ? "ar-EG" : "en-US"));
  return (
    <div>
      <PageHead
        title={ar ? "المالية والفوترة — ذمم المنشأة" : "Finance & billing — facility receivables"}
        sub={ar ? "المرفوض أولًا لتصحيحه، ثم المسودات الراكدة، ثم الذمم الأقدم. الفاتورة تُقفل بعد إصدارها، والإجمالي وحصص المؤمّن والمريض تُحسب خادميًا من البنود دائمًا." : "Rejected first for correction, then stale drafts, then the oldest receivables. Invoices lock after issue, and totals with insurer/patient shares are always computed server-side from the lines."}
        actions={<Button onClick={() => setNewInv(true)}>{ar ? "+ فاتورة جديدة" : "+ New invoice"}</Button>}
      />
      {err ? <ErrorBanner onRetry={load}>{err}</ErrorBanner> : null}
      <div className="kpi-grid">
        <KpiCard label={ar ? "ذمم مستحقة" : "Outstanding"} value={money(s.outstanding_total)} />
        <KpiCard label={ar ? "حصة المؤمّن" : "Insurer share"} value={money(s.outstanding_insurer)} />
        <KpiCard label={ar ? "حصة المرضى" : "Patient share"} value={money(s.outstanding_patient)} />
        <KpiCard label={ar ? "متأخرة +90 يومًا" : "Overdue 90+"} value={money(s.overdue_90_total)} />
        <KpiCard label={ar ? "مرفوضة تنتظر تصحيحًا" : "Rejected"} value={s.rejected ?? "—"} />
        <KpiCard label={ar ? "محصّل فعليًا" : "Collected"} value={money(s.collected_total)} />
      </div>
      <Card><h3 className="card-h">{ar ? "أعمار الذمم" : "Receivables aging"}</h3>
        <div className="row">
          {Object.keys(BUCKET_AR).map((b) => (
            <span key={b} className={`badge${b === "over_90" && (buckets[b] ?? 0) > 0 ? " danger" : ""}`}>{BUCKET_AR[b]}: {buckets[b] ?? 0}</span>
          ))}
          <span className="badge warn">{ar ? "مسودات راكدة" : "Stale drafts"}: {s.drafts_stale ?? 0}</span>
        </div>
      </Card>
      <Card><h3 className="card-h">{ar ? "طابور انتباه الفواتير" : "Invoice attention queue"}</h3>
        {!board ? <p className="muted">{ar ? "جارٍ التحميل…" : "Loading…"}</p>
          : board.invoices.length === 0 ? <EmptyState>{ar ? "لا فواتير تحتاج انتباهًا — كل الذمم مسوّاة أو حديثة." : "No invoice needs attention — receivables are settled or fresh."}</EmptyState>
          : board.invoices.map((inv) => (
            <div key={inv.id} className="row-card">
              <div className="grow-field">
                <strong>{money(inv.total)}</strong> <span className="muted">{inv.payer ?? "—"} · {STATUS_AR[inv.status] ?? inv.status} · {inv.age_days} {ar ? "يوم" : "d"} ({BUCKET_AR[inv.bucket] ?? inv.bucket})</span>
                <div className="muted">{ar ? "المؤمّن" : "Insurer"}: {money(inv.insurer_amount)} · {ar ? "المريض" : "Patient"}: {money(inv.patient_amount)}</div>
                {inv.status === "rejected" ? <span className="badge danger">{ar ? "مرفوضة — صحّح وأعد الإصدار" : "Rejected — correct and re-issue"}</span> : null}
                {inv.status === "draft" && inv.age_days > 7 ? <span className="badge warn">{ar ? "مسودة راكدة" : "Stale draft"}</span> : null}
              </div>
              {inv.status === "draft" ? <Button variant="soft" onClick={() => { setLineInv(inv); setLineForm({ description: "", unitPrice: "", quantity: "1", coveragePct: "80" }); }}>{ar ? "إضافة بند" : "Add line"}</Button> : null}
              {(NEXT[inv.status] ?? []).map((to) => (
                <Button key={to} variant={to === "cancelled" ? "danger" : "soft"} onClick={() => move(inv, to)}>{STATUS_AR[to] ?? to}</Button>
              ))}
            </div>
          ))}
      </Card>

      {newInv ? (
        <Drawer title={ar ? "فاتورة جديدة" : "New invoice"} onClose={() => setNewInv(false)}>
          <div className="field"><label>{ar ? "معرف المريض (UUID)" : "Patient id (UUID)"}</label>
            <input value={invForm.patientId} onChange={(e) => setInvForm({ ...invForm, patientId: e.target.value })} /></div>
          <div className="field"><label>{ar ? "الجهة الدافعة" : "Payer"}</label>
            <input value={invForm.payer} onChange={(e) => setInvForm({ ...invForm, payer: e.target.value })} placeholder={ar ? "تأمين صحي / نقدي / جهة…" : "Insurer / cash / …"} /></div>
          <Button onClick={createInv}>{ar ? "إنشاء مسودة" : "Create draft"}</Button>
        </Drawer>
      ) : null}
      {lineInv ? (
        <Drawer title={ar ? "إضافة بند — تُقفل الفاتورة بعد الإصدار" : "Add a line — the invoice locks once issued"} onClose={() => setLineInv(null)}>
          <div className="field"><label>{ar ? "الوصف" : "Description"}</label>
            <input value={lineForm.description} onChange={(e) => setLineForm({ ...lineForm, description: e.target.value })} /></div>
          <div className="field"><label>{ar ? "سعر الوحدة" : "Unit price"}</label>
            <input type="number" min="0" step="0.01" value={lineForm.unitPrice} onChange={(e) => setLineForm({ ...lineForm, unitPrice: e.target.value })} /></div>
          <div className="field"><label>{ar ? "الكمية" : "Quantity"}</label>
            <input type="number" min="1" value={lineForm.quantity} onChange={(e) => setLineForm({ ...lineForm, quantity: e.target.value })} /></div>
          <div className="field"><label>{ar ? "نسبة تغطية المؤمّن %" : "Insurer coverage %"}</label>
            <input type="number" min="0" max="100" value={lineForm.coveragePct} onChange={(e) => setLineForm({ ...lineForm, coveragePct: e.target.value })} /></div>
          <Button onClick={addLine}>{ar ? "إضافة وإعادة الحساب" : "Add & recompute"}</Button>
        </Drawer>
      ) : null}
      <Toasts items={toasts} />
    </div>
  );
}
