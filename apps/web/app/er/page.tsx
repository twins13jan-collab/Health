"use client";

import { useCallback, useEffect, useState } from "react";
import KpiCard from "@/components/KpiCard";
import { Button, Drawer, PageHead, Toasts } from "@/components/ui";
import { api } from "@/lib/api";
import { useLang } from "@/lib/i18n";
import type { Patient } from "@/lib/types";

interface ErRow {
  id: string; patientId: string; patientName?: string; nationalId?: string;
  chiefComplaint?: string; triageLevel?: number; news2Score?: number;
  status: string; arrivedAt?: string; waitMinutes: number; targetMinutes?: number; breached: boolean;
}
interface ErSummary { waiting: number; inTreatment: number; seenToday: number; breaches: number; longestWaitMinutes: number }
interface WardOpt { id: string; nameAr: string; free: number; active: boolean }

const LEVELS = [1, 2, 3, 4, 5];
const LEVEL_TONE = (level?: number) =>
  !level ? "" : level <= 2 ? "danger" : level === 3 ? "warn" : level === 4 ? "info" : "";

/** Emergency & triage board: the live priority queue for this facility's
 *  ER today — ordered by triage level and wait, with target breaches
 *  flagged. Arrival, (re-)triage with NEWS2 vitals, start of treatment,
 *  admission into ADT, and discharge all happen here. */
export default function ErPage() {
  const { lang } = useLang();
  const ar = lang === "ar";
  const [queue, setQueue] = useState<ErRow[]>([]);
  const [summary, setSummary] = useState<ErSummary | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [toast, setToast] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const [drawer, setDrawer] = useState<null | { kind: "arrival" } | { kind: "triage" | "admit"; row: ErRow }>(null);
  const [wards, setWards] = useState<WardOpt[]>([]);
  const [patientQuery, setPatientQuery] = useState("");
  const [patientResults, setPatientResults] = useState<Patient[]>([]);
  const [arrival, setArrival] = useState({ patientId: "", patientName: "", chiefComplaint: "", triageLevel: "3" });
  const [vitals, setVitals] = useState({ respiratoryRate: "", spo2: "", onOxygen: false, systolicBp: "", pulse: "", temperature: "", consciousness: "alert" });
  const [triageLevel, setTriageLevel] = useState("3");
  const [admitForm, setAdmitForm] = useState({ wardId: "", bedLabel: "", attendingName: "", admissionReason: "" });

  const load = useCallback(() => {
    api.getJson<{ queue: ErRow[]; summary: ErSummary }>("/api/encounters/er-board")
      .then((r) => { setQueue(r.queue); setSummary(r.summary); setError(null); })
      .catch((e) => setError(e instanceof Error ? e.message : String(e)));
  }, []);
  useEffect(() => {
    load();
    const id = setInterval(load, 30000);
    return () => clearInterval(id);
  }, [load]);
  useEffect(() => {
    if (!toast) return;
    const id = setTimeout(() => setToast(null), 4000);
    return () => clearTimeout(id);
  }, [toast]);

  async function searchPatients(q: string) {
    setPatientQuery(q);
    if (q.trim().length < 2) { setPatientResults([]); return; }
    try {
      const r = await api.patients(q.trim());
      setPatientResults(r.data.slice(0, 8));
    } catch { setPatientResults([]); }
  }

  function vitalsBody() {
    if (!vitals.respiratoryRate) return null;
    return {
      respiratoryRate: Number(vitals.respiratoryRate),
      spo2: vitals.spo2 ? Number(vitals.spo2) : null,
      onOxygen: vitals.onOxygen,
      systolicBp: vitals.systolicBp ? Number(vitals.systolicBp) : null,
      pulse: vitals.pulse ? Number(vitals.pulse) : null,
      temperature: vitals.temperature ? Number(vitals.temperature) : null,
      consciousness: vitals.consciousness,
    };
  }

  async function submitArrival(ev: React.FormEvent) {
    ev.preventDefault();
    setBusy(true); setError(null);
    try {
      await api.postJson("/api/encounters", {
        patientId: arrival.patientId, type: "emergency",
        chiefComplaint: arrival.chiefComplaint || null,
        triageLevel: Number(arrival.triageLevel), vitals: vitalsBody(),
      });
      setDrawer(null);
      setToast(ar ? "تم تسجيل وصول جديد" : "Arrival registered");
      load();
    } catch (e) { setError(e instanceof Error ? e.message : String(e)); } finally { setBusy(false); }
  }

  async function submitTriage(ev: React.FormEvent) {
    ev.preventDefault();
    if (!drawer || drawer.kind !== "triage") return;
    setBusy(true); setError(null);
    try {
      const r = await api.postJson<{ news2?: { total: number } }>(`/api/encounters/${drawer.row.id}/triage`, {
        triageLevel: Number(triageLevel), vitals: vitalsBody(),
      });
      setDrawer(null);
      setToast(ar ? `تم الفرز — المستوى ${triageLevel}${r.news2 ? ` · NEWS2 = ${r.news2.total}` : ""}` : `Triaged — level ${triageLevel}`);
      load();
    } catch (e) { setError(e instanceof Error ? e.message : String(e)); } finally { setBusy(false); }
  }

  async function setStatus(row: ErRow, status: string, msg: string) {
    setBusy(true); setError(null);
    try {
      await api.postJson(`/api/encounters/${row.id}/status`, { status });
      setToast(msg);
      load();
    } catch (e) { setError(e instanceof Error ? e.message : String(e)); } finally { setBusy(false); }
  }

  async function openAdmit(row: ErRow) {
    try {
      const w = await api.moduleList<WardOpt>("/api/adt/wards", "wards");
      setWards(w.data.filter((x) => x.active));
    } catch { setWards([]); }
    setAdmitForm({ wardId: "", bedLabel: "", attendingName: "", admissionReason: row.chiefComplaint ?? "" });
    setDrawer({ kind: "admit", row });
  }

  async function submitAdmit(ev: React.FormEvent) {
    ev.preventDefault();
    if (!drawer || drawer.kind !== "admit") return;
    setBusy(true); setError(null);
    try {
      await api.postJson("/api/adt/admissions", {
        patientId: drawer.row.patientId, encounterId: drawer.row.id,
        wardId: admitForm.wardId, bedLabel: admitForm.bedLabel,
        attendingName: admitForm.attendingName || null,
        admissionReason: admitForm.admissionReason || null, source: "er",
      });
      await api.postJson(`/api/encounters/${drawer.row.id}/status`, { status: "completed" });
      setDrawer(null);
      setToast(ar ? "تم تنويم المريض من الطوارئ" : "Patient admitted from ER");
      load();
    } catch (e) { setError(e instanceof Error ? e.message : String(e)); } finally { setBusy(false); }
  }

  const vitalsFields = (
    <div className="form-grid">
      <div className="field"><label>{ar ? "معدل التنفس" : "Respiratory rate"}</label><input type="number" value={vitals.respiratoryRate} onChange={(e) => setVitals({ ...vitals, respiratoryRate: e.target.value })} /></div>
      <div className="field"><label>SpO₂ %</label><input type="number" value={vitals.spo2} onChange={(e) => setVitals({ ...vitals, spo2: e.target.value })} /></div>
      <div className="field"><label>{ar ? "ضغط الدم الانقباضي" : "Systolic BP"}</label><input type="number" value={vitals.systolicBp} onChange={(e) => setVitals({ ...vitals, systolicBp: e.target.value })} /></div>
      <div className="field"><label>{ar ? "النبض" : "Pulse"}</label><input type="number" value={vitals.pulse} onChange={(e) => setVitals({ ...vitals, pulse: e.target.value })} /></div>
      <div className="field"><label>{ar ? "الحرارة" : "Temperature"}</label><input type="number" step="0.1" value={vitals.temperature} onChange={(e) => setVitals({ ...vitals, temperature: e.target.value })} /></div>
      <div className="field"><label>{ar ? "الوعي" : "Consciousness"}</label>
        <select value={vitals.consciousness} onChange={(e) => setVitals({ ...vitals, consciousness: e.target.value })}>
          <option value="alert">alert</option><option value="voice">voice</option><option value="pain">pain</option><option value="unresponsive">unresponsive</option>
        </select></div>
      <div className="field"><label>{ar ? "على أكسجين" : "On oxygen"}</label>
        <select value={vitals.onOxygen ? "true" : "false"} onChange={(e) => setVitals({ ...vitals, onOxygen: e.target.value === "true" })}>
          <option value="false">{ar ? "لا" : "No"}</option><option value="true">{ar ? "نعم" : "Yes"}</option>
        </select></div>
    </div>
  );

  return (
    <>
      <PageHead
        eyebrow={ar ? "الطوارئ" : "Emergency"}
        title={ar ? "الطوارئ والفرز" : "Emergency & Triage"}
        sub={ar ? "طابور الأولوية الحي لطوارئ منشأتك اليوم — مرتبًا بمستوى الفرز وزمن الانتظار، مع تنبيه تجاوز الأزمنة المستهدفة. تتحدث اللوحة تلقائيًا كل ٣٠ ثانية" : "Your facility's live ER priority queue for today — ordered by triage level and wait, with target-breach flags. Refreshes every 30 seconds"}
        actions={
          <>
            <Button onClick={() => { setArrival({ patientId: "", patientName: "", chiefComplaint: "", triageLevel: "3" }); setPatientQuery(""); setPatientResults([]); setDrawer({ kind: "arrival" }); }}>＋ {ar ? "وصول جديد" : "New arrival"}</Button>
            <Button variant="soft" onClick={load}>{ar ? "تحديث" : "Refresh"}</Button>
          </>
        }
      />
      {error ? <div className="alert-error">{error}</div> : null}

      <div className="kpi-grid">
        <KpiCard label={ar ? "في الانتظار الآن" : "Waiting now"} value={summary?.waiting ?? "…"} tone="warn" />
        <KpiCard label={ar ? "قيد الكشف والعلاج" : "In treatment"} value={summary?.inTreatment ?? "…"} />
        <KpiCard label={ar ? "وصول اليوم" : "Arrivals today"} value={summary?.seenToday ?? "…"} />
        <KpiCard label={ar ? "تجاوزات زمن مستهدف" : "Target breaches"} value={summary?.breaches ?? "…"} tone="danger" />
        <KpiCard label={ar ? "أطول انتظار (دقيقة)" : "Longest wait (min)"} value={summary?.longestWaitMinutes ?? "…"} />
      </div>

      <section className="card">
        <h2>{ar ? "طابور الطوارئ" : "ER queue"}</h2>
        {queue.length === 0 ? (
          <div className="empty">{ar ? "لا حالات طوارئ اليوم حتى الآن" : "No emergency arrivals yet today"}</div>
        ) : (
          <div className="tbl-wrap">
            <table className="tbl">
              <thead><tr>
                <th>{ar ? "الفرز" : "Triage"}</th><th>{ar ? "المريض" : "Patient"}</th><th>{ar ? "الشكوى" : "Complaint"}</th>
                <th>NEWS2</th><th>{ar ? "الوصول" : "Arrived"}</th><th>{ar ? "الانتظار" : "Wait"}</th>
                <th>{ar ? "الحالة" : "Status"}</th><th>{ar ? "إجراءات" : "Actions"}</th>
              </tr></thead>
              <tbody>
                {queue.map((row) => (
                  <tr key={row.id} className={row.breached ? "alert-row" : undefined}>
                    <td><span className={`badge ${LEVEL_TONE(row.triageLevel)}`}>{row.triageLevel ? `${ar ? "مستوى" : "L"}${row.triageLevel}` : "—"}</span></td>
                    <td><strong>{row.patientName ?? row.patientId}</strong>{row.nationalId ? <div className="small muted" dir="ltr">{row.nationalId}</div> : null}</td>
                    <td>{row.chiefComplaint ?? "—"}</td>
                    <td>{row.news2Score ?? "—"}</td>
                    <td>{row.arrivedAt ? row.arrivedAt.slice(11, 16) : "—"}</td>
                    <td>
                      <span className={row.breached ? "strong" : undefined}>
                        {row.waitMinutes} {ar ? "د" : "min"}{row.breached ? " ⚠" : ""}
                      </span>
                      {row.targetMinutes !== undefined && row.targetMinutes !== null ? <div className="muted small">{ar ? "المستهدف" : "target"} {row.targetMinutes} {ar ? "د" : "min"}</div> : null}
                    </td>
                    <td>{row.status === "open" ? (ar ? "منتظر" : "Waiting") : row.status === "in_progress" ? (ar ? "قيد الكشف" : "In treatment") : row.status}</td>
                    <td className="nowrap">
                      <span className="row">
                        {row.status === "open" ? <Button small disabled={busy} onClick={() => setStatus(row, "in_progress", ar ? "بدأ الكشف" : "Treatment started")}>{ar ? "بدء الكشف" : "Start"}</Button> : null}
                        {(row.status === "open" || row.status === "in_progress") ? (
                          <>
                            <Button variant="soft" small disabled={busy} onClick={() => { setTriageLevel(String(row.triageLevel ?? 3)); setDrawer({ kind: "triage", row }); }}>{ar ? "إعادة فرز" : "Re-triage"}</Button>
                            <Button variant="soft" small disabled={busy} onClick={() => openAdmit(row)}>{ar ? "تنويم" : "Admit"}</Button>
                            <Button variant="soft" small disabled={busy} onClick={() => setStatus(row, "completed", ar ? "انتهت الحالة وخرجت" : "Completed & discharged")}>{ar ? "إنهاء وخروج" : "Complete"}</Button>
                          </>
                        ) : null}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      {drawer?.kind === "arrival" ? (
        <Drawer title={ar ? "وصول جديد للطوارئ" : "New ER arrival"} onClose={() => setDrawer(null)}>
          <form onSubmit={submitArrival}>
            <div className="field">
              <label>{ar ? "بحث المريض" : "Patient search"}</label>
              <input value={patientQuery} onChange={(e) => searchPatients(e.target.value)} placeholder={ar ? "الاسم أو الرقم القومي…" : "Name or national ID…"} />
              {patientResults.length > 0 ? (
                <div className="picker">
                  {patientResults.map((pt) => (
                    <button key={pt.id} type="button" className="picker-item"
                      onClick={() => { setArrival({ ...arrival, patientId: pt.id, patientName: pt.fullNameAr }); setPatientResults([]); setPatientQuery(pt.fullNameAr); }}>
                      {pt.fullNameAr} <span className="muted" dir="ltr">{pt.nationalId}</span>
                    </button>
                  ))}
                </div>
              ) : null}
              {arrival.patientId ? <div className="picked-note">✓ {arrival.patientName}</div> : null}
            </div>
            <div className="form-grid one">
              <div className="field"><label>{ar ? "الشكوى الرئيسية" : "Chief complaint"}</label><input value={arrival.chiefComplaint} onChange={(e) => setArrival({ ...arrival, chiefComplaint: e.target.value })} /></div>
              <div className="field"><label>{ar ? "مستوى الفرز" : "Triage level"}</label>
                <select value={arrival.triageLevel} onChange={(e) => setArrival({ ...arrival, triageLevel: e.target.value })}>
                  {LEVELS.map((l) => <option key={l} value={l}>{l}</option>)}
                </select></div>
            </div>
            <h3 className="mt mb">{ar ? "العلامات الحيوية (NEWS2 — اختياري)" : "Vitals (NEWS2 — optional)"}</h3>
            {vitalsFields}
            <div className="form-actions">
              <button type="submit" className="btn" disabled={busy || !arrival.patientId}>{ar ? "تسجيل الوصول" : "Register arrival"}</button>
              <button type="button" className="btn btn-ghost" onClick={() => setDrawer(null)}>{ar ? "إلغاء" : "Cancel"}</button>
            </div>
          </form>
        </Drawer>
      ) : null}

      {drawer?.kind === "triage" ? (
        <Drawer title={`${ar ? "إعادة فرز" : "Re-triage"} — ${drawer.row.patientName ?? ""}`} onClose={() => setDrawer(null)}>
          <form onSubmit={submitTriage}>
            <div className="field"><label>{ar ? "مستوى الفرز الجديد" : "New triage level"}</label>
              <select value={triageLevel} onChange={(e) => setTriageLevel(e.target.value)}>
                {LEVELS.map((l) => <option key={l} value={l}>{l}</option>)}
              </select></div>
            {vitalsFields}
            <div className="form-actions">
              <button type="submit" className="btn" disabled={busy}>{ar ? "حفظ الفرز" : "Save triage"}</button>
              <button type="button" className="btn btn-ghost" onClick={() => setDrawer(null)}>{ar ? "إلغاء" : "Cancel"}</button>
            </div>
          </form>
        </Drawer>
      ) : null}

      {drawer?.kind === "admit" ? (
        <Drawer title={`${ar ? "تنويم من الطوارئ" : "Admit from ER"} — ${drawer.row.patientName ?? ""}`} onClose={() => setDrawer(null)}>
          <form onSubmit={submitAdmit}>
            <div className="form-grid one">
              <div className="field"><label>{ar ? "الجناح" : "Ward"}</label>
                <select required value={admitForm.wardId} onChange={(e) => setAdmitForm({ ...admitForm, wardId: e.target.value })}>
                  <option value="">—</option>
                  {wards.map((w) => <option key={w.id} value={w.id}>{w.nameAr} ({w.free} {ar ? "شاغر" : "free"})</option>)}
                </select></div>
              <div className="field"><label>{ar ? "السرير" : "Bed"}</label><input required value={admitForm.bedLabel} onChange={(e) => setAdmitForm({ ...admitForm, bedLabel: e.target.value })} /></div>
              <div className="field"><label>{ar ? "الطبيب المعالج" : "Attending"}</label><input value={admitForm.attendingName} onChange={(e) => setAdmitForm({ ...admitForm, attendingName: e.target.value })} /></div>
              <div className="field"><label>{ar ? "سبب التنويم" : "Reason"}</label><input value={admitForm.admissionReason} onChange={(e) => setAdmitForm({ ...admitForm, admissionReason: e.target.value })} /></div>
            </div>
            <div className="form-actions">
              <button type="submit" className="btn" disabled={busy}>{ar ? "تنويم" : "Admit"}</button>
              <button type="button" className="btn btn-ghost" onClick={() => setDrawer(null)}>{ar ? "إلغاء" : "Cancel"}</button>
            </div>
          </form>
        </Drawer>
      ) : null}

      <Toasts items={toast ? [{ text: toast }] : []} />
    </>
  );
}
