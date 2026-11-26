"use client";

import { useCallback, useEffect, useState } from "react";
import KpiCard from "@/components/KpiCard";
import { Button, Drawer, PageHead, SectionHead, Toasts } from "@/components/ui";
import { api } from "@/lib/api";
import { useLang } from "@/lib/i18n";

interface RadRow { id: string; patient_id: string; patient_name: string; modality: string; body_part?: string; clinical_question?: string; priority: string; status: string; stage: string; waiting_minutes: number; minutes_since_imaged?: number; report_overdue?: boolean; sla_minutes?: number; report_text?: string }
interface CritRow { id: string; order_id: string; finding: string; minutes_waiting: number }
interface BoardResp { acquisition: RadRow[]; reporting: RadRow[]; verification: RadRow[]; criticals: CritRow[]; summary: { waiting_imaging: number; in_progress: number; awaiting_report: number; report_overdue: number; awaiting_verification: number; verified_today: number; unacknowledged_criticals: number; waiting_by_modality: Record<string, number> } }

const PRIO_TONE: Record<string, string> = { stat: "danger", emergency: "danger", urgent: "warn", routine: "info" };
const NEXT_STEP: Record<string, { to: string; ar: string; en: string }> = {
  ordered: { to: "scheduled", ar: "جدولة الفحص", en: "Schedule" },
  scheduled: { to: "in_progress", ar: "بدء التنفيذ", en: "Start exam" },
  in_progress: { to: "imaged", ar: "إتمام التصوير", en: "Complete imaging" },
};
const MODALITIES = ["XR", "CT", "MRI", "US", "MAMMO", "ANGIO"];

/** Radiology department worklist (Phase 4.8, module 93): two queues —
 *  acquisition (to be imaged) and reporting (imaged, awaiting a
 *  report) — each ordered by clinical priority then waiting time.
 *  Reporting carries a per-priority turnaround SLA; overdue studies
 *  are flagged, and unacknowledged critical results from the deep
 *  layer sit on top until a named receiver acknowledges them. */
export default function RadiologyPage() {
  const { lang, t } = useLang();
  const ar = lang === "ar";
  const [board, setBoard] = useState<BoardResp | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [toasts, setToasts] = useState<{ text: string; err?: boolean }[]>([]);
  const [busy, setBusy] = useState(false);
  const [reportFor, setReportFor] = useState<RadRow | null>(null);
  const [reportText, setReportText] = useState("");
  const [ackFor, setAckFor] = useState<CritRow | null>(null);
  const [receiver, setReceiver] = useState("");
  const [creating, setCreating] = useState(false);
  const [form, setForm] = useState({ patientId: "", modality: "XR", bodyPart: "", clinicalQuestion: "", priority: "routine" });

  const toast = (text: string, err?: boolean) => { setToasts((x) => [...x, { text, err }]); setTimeout(() => setToasts((x) => x.slice(1)), 4500); };

  const load = useCallback(async () => {
    try { setBoard(await api.getJson<BoardResp>("/api/radiology/board")); }
    catch (e) { setError(e instanceof Error ? e.message : String(e)); }
  }, []);
  useEffect(() => { load(); const iv = setInterval(load, 30000); return () => clearInterval(iv); }, [load]);

  async function act(fn: () => Promise<unknown>, okMsg: string) {
    setBusy(true);
    try { await fn(); toast(okMsg); await load(); }
    catch (e) { toast(e instanceof Error ? e.message : String(e), true); }
    finally { setBusy(false); }
  }

  const s = board?.summary;
  const prioBadge = (p: string) => <span className={`badge ${PRIO_TONE[p?.toLowerCase()] ?? "info"}`}>{p}</span>;

  return (
    <div>
      <PageHead eyebrow={ar ? "التشغيل السريري" : "Clinical operations"} title={t("radiology")}
        sub={ar ? "قائمتا عمل القسم: التصوير (بانتظار التنفيذ) والتقارير (تم تصويرها وبانتظار التقرير) — مرتبة بالأولوية السريرية ثم زمن الانتظار. للتقرير مهلة زمنية حسب الأولوية، والمتأخر يُعلَّم صراحة، والنتيجة الحرجة لا تختفي حتى يستلمها مستقبل مسمى."
               : "The department's two worklists — acquisition and reporting — ordered by clinical priority then waiting time. Reports carry a per-priority SLA; overdue studies are flagged, and a critical result stays visible until a named receiver acknowledges it."}
        actions={<>
          <Button onClick={() => setCreating(true)}>{ar ? "طلب تصوير جديد" : "New imaging order"}</Button>
          <Button onClick={load}>{ar ? "تحديث" : "Refresh"}</Button>
        </>} />
      {error ? <div className="alert-error">{error}</div> : null}
      <div className="kpi-grid">
        <KpiCard label={ar ? "بانتظار التصوير" : "Waiting imaging"} value={s?.waiting_imaging ?? "—"} />
        <KpiCard label={ar ? "قيد التنفيذ" : "In progress"} value={s?.in_progress ?? "—"} />
        <KpiCard label={ar ? "بانتظار التقرير" : "Awaiting report"} value={s?.awaiting_report ?? "—"} />
        <KpiCard label={ar ? "تقارير متأخرة عن المهلة" : "Reports overdue"} value={s?.report_overdue ?? "—"} />
        <KpiCard label={ar ? "نتائج حرجة بلا استلام" : "Criticals unacknowledged"} value={s?.unacknowledged_criticals ?? "—"} />
        <KpiCard label={ar ? "تم التحقق اليوم" : "Verified today"} value={s?.verified_today ?? "—"} />
      </div>

      {!board ? <div className="empty">{ar ? "جارٍ التحميل…" : "Loading…"}</div> : (
        <>
          {board.criticals.length > 0 ? (
            <>
              <SectionHead eyebrow={ar ? "تنبيه" : "Alert"} title={ar ? "نتائج حرجة بانتظار استلام موثق" : "Critical results awaiting documented receipt"} />
              <div className="cards-grid">
                {board.criticals.map((c) => (
                  <section key={c.id} className="card">
                    <div className="l-row">
                      <strong>{c.finding}</strong>
                      <span className="badge danger">{ar ? "حرجة" : "Critical"}</span>
                      <span className="badge warn">{ar ? "منذ" : "waiting"} {c.minutes_waiting} {ar ? "دقيقة" : "min"}</span>
                    </div>
                    <div className="l-actions">
                      <Button disabled={busy} onClick={() => { setAckFor(c); setReceiver(""); }}>{ar ? "توثيق الاستلام" : "Acknowledge receipt"}</Button>
                    </div>
                  </section>
                ))}
              </div>
            </>
          ) : null}

          <SectionHead eyebrow={ar ? "قائمة العمل" : "Worklist"} title={ar ? "بانتظار التصوير" : "Acquisition queue"} />
          {board.acquisition.length === 0 ? <div className="empty">{ar ? "لا فحوص بانتظار التصوير" : "No studies waiting to be imaged"}</div> : (
            <div className="cards-grid">
              {board.acquisition.map((r) => (
                <section key={r.id} className="card">
                  <div className="l-row">
                    <strong>{r.patient_name || r.patient_id}</strong>
                    {prioBadge(r.priority)}
                    <span className="badge info">{r.modality}{r.body_part ? ` · ${r.body_part}` : ""}</span>
                    <span className="badge warn">{ar ? "انتظار" : "waiting"} {r.waiting_minutes} {ar ? "د" : "min"}</span>
                  </div>
                  {r.clinical_question ? <div className="l-row small">{r.clinical_question}</div> : null}
                  <div className="l-actions">
                    {NEXT_STEP[r.status] ? (
                      <Button disabled={busy} onClick={() => act(() => api.postJson(`/api/radiology/orders/${r.id}/status`, { status: NEXT_STEP[r.status].to }),
                        ar ? NEXT_STEP[r.status].ar : NEXT_STEP[r.status].en)}>
                        {ar ? NEXT_STEP[r.status].ar : NEXT_STEP[r.status].en}
                      </Button>
                    ) : null}
                  </div>
                </section>
              ))}
            </div>
          )}

          <SectionHead eyebrow={ar ? "قائمة العمل" : "Worklist"} title={ar ? "بانتظار التقرير" : "Reporting queue"} />
          {board.reporting.length === 0 ? <div className="empty">{ar ? "لا فحوص مصوَّرة بانتظار التقرير" : "No imaged studies awaiting a report"}</div> : (
            <div className="cards-grid">
              {board.reporting.map((r) => (
                <section key={r.id} className="card">
                  <div className="l-row">
                    <strong>{r.patient_name || r.patient_id}</strong>
                    {prioBadge(r.priority)}
                    <span className="badge info">{r.modality}{r.body_part ? ` · ${r.body_part}` : ""}</span>
                    {r.report_overdue
                      ? <span className="badge danger">{ar ? "متأخر عن المهلة" : "Overdue"} · {r.minutes_since_imaged} {ar ? "د" : "min"} / SLA {r.sla_minutes}</span>
                      : <span className="badge warn">{ar ? "منذ التصوير" : "imaged"} {r.minutes_since_imaged} {ar ? "د" : "min"} / SLA {r.sla_minutes}</span>}
                  </div>
                  {r.clinical_question ? <div className="l-row small">{r.clinical_question}</div> : null}
                  <div className="l-actions">
                    <Button disabled={busy} onClick={() => { setReportFor(r); setReportText(""); }}>{ar ? "كتابة التقرير" : "Write report"}</Button>
                  </div>
                </section>
              ))}
            </div>
          )}

          <SectionHead eyebrow={ar ? "الاعتماد" : "Verification"} title={ar ? "تقارير بانتظار التحقق" : "Reports awaiting verification"} />
          {board.verification.length === 0 ? <div className="empty">{ar ? "لا تقارير بانتظار التحقق" : "Nothing awaiting verification"}</div> : (
            <div className="cards-grid">
              {board.verification.map((r) => (
                <section key={r.id} className="card">
                  <div className="l-row">
                    <strong>{r.patient_name || r.patient_id}</strong>
                    {prioBadge(r.priority)}
                    <span className="badge info">{r.modality}{r.body_part ? ` · ${r.body_part}` : ""}</span>
                  </div>
                  {r.report_text ? <div className="l-row small">{r.report_text}</div> : null}
                  <div className="l-actions">
                    <Button disabled={busy} onClick={() => act(() => api.postJson(`/api/radiology/orders/${r.id}/status`, { status: "verified" }),
                      ar ? "تم التحقق من التقرير واعتماده" : "Report verified")}>
                      {ar ? "تحقق واعتماد" : "Verify report"}
                    </Button>
                  </div>
                </section>
              ))}
            </div>
          )}
        </>
      )}

      {reportFor ? (
        <Drawer title={ar ? `تقرير — ${reportFor.patient_name || reportFor.patient_id}` : `Report — ${reportFor.patient_name || reportFor.patient_id}`} onClose={() => setReportFor(null)}>
          <div className="form-grid">
            <div className="field"><label>{ar ? "نص التقرير" : "Report text"}</label>
              <textarea rows={6} value={reportText} onChange={(e) => setReportText(e.target.value)} /></div>
          </div>
          <div className="form-actions">
            <Button disabled={busy || !reportText.trim()} onClick={() => act(async () => {
              await api.postJson(`/api/radiology/orders/${reportFor.id}/report`, { report_text: reportText });
              setReportFor(null);
            }, ar ? "تم حفظ التقرير" : "Report saved")}>{ar ? "حفظ التقرير" : "Save report"}</Button>
          </div>
        </Drawer>
      ) : null}

      {ackFor ? (
        <Drawer title={ar ? "توثيق استلام نتيجة حرجة" : "Acknowledge critical result"} onClose={() => setAckFor(null)}>
          <p>{ackFor.finding}</p>
          <div className="form-grid">
            <div className="field"><label>{ar ? "اسم المستلم" : "Receiver name"}</label>
              <input value={receiver} onChange={(e) => setReceiver(e.target.value)} /></div>
          </div>
          <div className="form-actions">
            <Button disabled={busy || !receiver.trim()} onClick={() => act(async () => {
              await api.postJson(`/api/radiology-deep/critical-results/${ackFor.id}/acknowledge`, { acknowledgedBy: receiver });
              setAckFor(null);
            }, ar ? "تم توثيق الاستلام" : "Receipt acknowledged")}>{ar ? "توثيق الاستلام" : "Acknowledge"}</Button>
          </div>
        </Drawer>
      ) : null}

      {creating ? (
        <Drawer title={ar ? "طلب تصوير جديد" : "New imaging order"} onClose={() => setCreating(false)}>
          <div className="form-grid">
            <div className="field"><label>{ar ? "معرف المريض" : "Patient ID"}</label>
              <input value={form.patientId} onChange={(e) => setForm({ ...form, patientId: e.target.value })} /></div>
            <div className="field"><label>{ar ? "الوسيلة" : "Modality"}</label>
              <select value={form.modality} onChange={(e) => setForm({ ...form, modality: e.target.value })}>
                {MODALITIES.map((m) => <option key={m} value={m}>{m}</option>)}
              </select></div>
            <div className="field"><label>{ar ? "الموضع" : "Body part"}</label>
              <input value={form.bodyPart} onChange={(e) => setForm({ ...form, bodyPart: e.target.value })} /></div>
            <div className="field"><label>{ar ? "الأولوية" : "Priority"}</label>
              <select value={form.priority} onChange={(e) => setForm({ ...form, priority: e.target.value })}>
                <option value="routine">routine</option>
                <option value="urgent">urgent</option>
                <option value="stat">stat</option>
              </select></div>
            <div className="field"><label>{ar ? "السؤال السريري (إلزامي — تبرير الإشعاع)" : "Clinical question (required — radiation justification)"}</label>
              <textarea rows={3} value={form.clinicalQuestion} onChange={(e) => setForm({ ...form, clinicalQuestion: e.target.value })} /></div>
          </div>
          <div className="form-actions">
            <Button disabled={busy || !form.patientId.trim() || !form.clinicalQuestion.trim()} onClick={() => act(async () => {
              await api.postJson("/api/radiology/orders", { patientId: form.patientId, modality: form.modality, bodyPart: form.bodyPart, clinicalQuestion: form.clinicalQuestion, priority: form.priority });
              setCreating(false);
            }, ar ? "تم إنشاء الطلب" : "Order created")}>{ar ? "إنشاء الطلب" : "Create order"}</Button>
          </div>
        </Drawer>
      ) : null}

      <Toasts items={toasts} />
    </div>
  );
}
