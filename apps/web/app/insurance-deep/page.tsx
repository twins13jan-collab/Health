"use client";

import { useCallback, useEffect, useState } from "react";
import { api } from "@/lib/api";
import { useLang } from "@/lib/i18n";
import { Button, Card, Drawer, EmptyState, ErrorBanner, PageHead, Toasts } from "@/components/ui";
import KpiCard from "@/components/KpiCard";

type ClaimRow = {
  id: string; patient_ref: string | null; service_ref: string | null; state: string; rank: number;
  gaps: string[]; adjudicable: boolean; settleable: boolean; medical_necessity: boolean;
  coverage_decision: string | null; fraud_flag: boolean; fraud_reviewed: boolean; age_days: number;
};
type Board = { claims: ClaimRow[]; summary: Record<string, number> };

const STATE_AR: Record<string, string> = { fraud_review_pending: "إشارة احتيال بانتظار مراجعة بشرية", appeal_open: "استئناف مفتوح", not_adjudicable: "لا تُفصل بعد — نواقص موثقة", awaiting_decision: "بانتظار قرار التغطية", awaiting_settlement: "بانتظار التسوية" };
const GAP_AR: Record<string, string> = { documentation_incomplete: "التوثيق غير مكتمل", tariff_mismatch: "لا تطابق مع التعريفة" };

export default function ClaimsBoardPage() {
  const { lang } = useLang();
  const ar = lang === "ar";
  const [board, setBoard] = useState<Board | null>(null);
  const [err, setErr] = useState("");
  const [toasts, setToasts] = useState<{ text: string; err?: boolean }[]>([]);
  const [newClaim, setNewClaim] = useState(false);
  const [appealClaim, setAppealClaim] = useState<ClaimRow | null>(null);
  const [grounds, setGrounds] = useState("");
  const [form, setForm] = useState({ patientRef: "", serviceRef: "", documentationComplete: true, tariffMatch: true, medicalNecessity: true, fraudFlag: false });

  const toast = (text: string, isErr = false) => setToasts((t) => [...t, { text, err: isErr }]);
  const load = useCallback(async () => {
    try { setBoard(await api.getJson<Board>("/api/insurance-deep/board")); setErr(""); }
    catch (e) { setErr(e instanceof Error ? e.message : String(e)); }
  }, []);
  useEffect(() => {
    load();
    const id = window.setInterval(load, 60000);
    return () => window.clearInterval(id);
  }, [load]);

  const act = async (path: string, body: unknown, okMsg: string) => {
    try { await api.postJson(path, body); toast(okMsg); load(); }
    catch (e) { toast(e instanceof Error ? e.message : String(e), true); }
  };
  const create = async () => {
    try {
      await api.postJson("/api/insurance-deep/claims", form);
      toast(ar ? "أُنشئت المطالبة" : "Claim created");
      setNewClaim(false); load();
    } catch (e) { toast(e instanceof Error ? e.message : String(e), true); }
  };
  const appeal = async () => {
    if (!appealClaim) return;
    await act(`/api/insurance-deep/claims/${appealClaim.id}/appeals`, { grounds }, ar ? "فُتح الاستئناف" : "Appeal opened");
    setAppealClaim(null);
  };

  const s = board?.summary ?? {};
  return (
    <div>
      <PageHead
        title={ar ? "مطالبات التأمين — الفصل والتسوية" : "Insurance claims — adjudication & settlement"}
        sub={ar ? "إشارة الاحتيال اشتباه لا حكم: يراجعها إنسان قبل أي تسوية. الضرورة الطبية تُسجل مستقلة عن قرار التغطية — رفض الدافع لا يعيد كتابتها. والمطالبة لا تُفصل قبل مطابقة الخدمة المنفذة مع التوثيق والتعريفة." : "A fraud flag is a suspicion, not a verdict: a human reviews it before any settlement. Medical necessity is recorded independently of the coverage decision — a denial never rewrites it. And no claim is adjudicated before the performed service matches documentation and tariff."}
        actions={<Button onClick={() => setNewClaim(true)}>{ar ? "+ مطالبة جديدة" : "+ New claim"}</Button>}
      />
      {err ? <ErrorBanner onRetry={load}>{err}</ErrorBanner> : null}
      <div className="kpi-grid">
        <KpiCard label={ar ? "مطالبات تحتاج انتباهًا" : "Claims needing attention"} value={s.attention_claims ?? "—"} />
        <KpiCard label={ar ? "احتيال بانتظار مراجعة" : "Fraud review pending"} value={s.fraud_review_pending ?? "—"} />
        <KpiCard label={ar ? "استئنافات مفتوحة" : "Open appeals"} value={s.appeals_open ?? "—"} />
        <KpiCard label={ar ? "لا تُفصل (نواقص)" : "Not adjudicable"} value={s.not_adjudicable ?? "—"} />
        <KpiCard label={ar ? "بانتظار التسوية" : "Awaiting settlement"} value={s.awaiting_settlement ?? "—"} />
        <KpiCard label={ar ? "مسوّاة" : "Settled"} value={s.settled ?? "—"} />
      </div>
      <Card><h3 className="card-h">{ar ? "طابور انتباه المطالبات" : "Claims attention queue"}</h3>
        {!board ? <p className="muted">{ar ? "جارٍ التحميل…" : "Loading…"}</p>
          : board.claims.length === 0 ? <EmptyState>{ar ? "لا مطالبات معلقة — كل المطالبات مسوّاة." : "No pending claims — everything is settled."}</EmptyState>
          : board.claims.map((c) => (
            <div key={c.id} className="row-card">
              <div className="grow-field">
                <strong>{c.patient_ref ?? "—"}</strong> <span className="muted">{c.service_ref ?? ""} · {STATE_AR[c.state] ?? c.state} · {c.age_days} {ar ? "يوم" : "d"}</span>
                <div>
                  {c.gaps.map((g) => <span key={g} className="badge warn">{GAP_AR[g] ?? g}</span>)}
                  {c.fraud_flag && !c.fraud_reviewed ? <span className="badge danger">{ar ? "إشارة احتيال غير مراجعة — التسوية موقوفة" : "Unreviewed fraud flag — settlement blocked"}</span> : null}
                  {c.coverage_decision ? <span className="badge">{ar ? "القرار" : "Decision"}: {c.coverage_decision}</span> : null}
                  <span className="badge">{ar ? "الضرورة الطبية" : "Medical necessity"}: {c.medical_necessity ? (ar ? "موثقة" : "documented") : (ar ? "غير موثقة" : "not documented")}</span>
                </div>
              </div>
              {c.state === "fraud_review_pending" ? <Button onClick={() => act(`/api/insurance-deep/claims/${c.id}/review-fraud`, {}, ar ? "رُوجعت الإشارة بشريًا" : "Fraud flag human-reviewed")}>{ar ? "مراجعة الاحتيال" : "Review fraud"}</Button> : null}
              {c.state === "awaiting_decision" ? <Button variant="soft" onClick={() => act(`/api/insurance-deep/claims/${c.id}/decide`, { decision: "approved" }, ar ? "اعتُمدت التغطية" : "Coverage approved")}>{ar ? "اعتماد" : "Approve"}</Button> : null}
              {c.state === "awaiting_decision" ? <Button variant="danger" onClick={() => act(`/api/insurance-deep/claims/${c.id}/decide`, { decision: "denied" }, ar ? "رُفضت التغطية — الضرورة الطبية تبقى كما سُجلت" : "Coverage denied — medical necessity stands as recorded")}>{ar ? "رفض" : "Deny"}</Button> : null}
              {c.state === "awaiting_settlement" ? <Button onClick={() => act(`/api/insurance-deep/claims/${c.id}/settle`, {}, ar ? "سُوّيت المطالبة" : "Claim settled")}>{ar ? "تسوية" : "Settle"}</Button> : null}
              <Button variant="soft" onClick={() => { setAppealClaim(c); setGrounds(""); }}>{ar ? "استئناف" : "Appeal"}</Button>
            </div>
          ))}
      </Card>

      {newClaim ? (
        <Drawer title={ar ? "مطالبة تأمين جديدة" : "New insurance claim"} onClose={() => setNewClaim(false)}>
          <div className="field"><label>{ar ? "مرجع المريض" : "Patient ref"}</label>
            <input value={form.patientRef} onChange={(e) => setForm({ ...form, patientRef: e.target.value })} /></div>
          <div className="field"><label>{ar ? "مرجع الخدمة" : "Service ref"}</label>
            <input value={form.serviceRef} onChange={(e) => setForm({ ...form, serviceRef: e.target.value })} /></div>
          <label className="check-row"><input type="checkbox" checked={form.documentationComplete} onChange={(e) => setForm({ ...form, documentationComplete: e.target.checked })} /> {ar ? "التوثيق مكتمل" : "Documentation complete"}</label>
          <label className="check-row"><input type="checkbox" checked={form.tariffMatch} onChange={(e) => setForm({ ...form, tariffMatch: e.target.checked })} /> {ar ? "مطابقة للتعريفة" : "Tariff match"}</label>
          <label className="check-row"><input type="checkbox" checked={form.medicalNecessity} onChange={(e) => setForm({ ...form, medicalNecessity: e.target.checked })} /> {ar ? "ضرورة طبية موثقة" : "Medical necessity documented"}</label>
          <label className="check-row"><input type="checkbox" checked={form.fraudFlag} onChange={(e) => setForm({ ...form, fraudFlag: e.target.checked })} /> {ar ? "إشارة احتيال" : "Fraud flag"}</label>
          <Button onClick={create}>{ar ? "إنشاء" : "Create"}</Button>
        </Drawer>
      ) : null}
      {appealClaim ? (
        <Drawer title={ar ? "فتح استئناف" : "Open an appeal"} onClose={() => setAppealClaim(null)}>
          <div className="field"><label>{ar ? "أسباب الاستئناف" : "Grounds"}</label>
            <input value={grounds} onChange={(e) => setGrounds(e.target.value)} /></div>
          <Button onClick={appeal}>{ar ? "فتح الاستئناف" : "Open appeal"}</Button>
        </Drawer>
      ) : null}
      <Toasts items={toasts} />
    </div>
  );
}
