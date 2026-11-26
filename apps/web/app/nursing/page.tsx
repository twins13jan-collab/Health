"use client";

import { useCallback, useEffect, useState } from "react";
import KpiCard from "@/components/KpiCard";
import { Button, PageHead, Toasts } from "@/components/ui";
import { api } from "@/lib/api";
import { useLang } from "@/lib/i18n";

interface Admission { id: string; patientId: string; bedLabel: string; attendingName?: string; admittedAt: string }
interface CensusRow { admission: Admission; patientName: string; wardName: string; dueDoses: number; overdueDoses: number; openTasks: number; lastNews2?: number | null; lastRoundAt?: string | null }
interface CensusResp { census: CensusRow[]; count: number; dueDoses: number; overdueDoses: number }
interface Dose { id: string; drugName: string; dose?: string; scheduledAt: string; status: string; reason?: string }
interface MarRow { administration: Dose; state: string }
interface Round { id: string; roundAt: string; news2Total?: number | null; news2Band?: string | null; painScore?: number | null; spo2?: number | null; pulse?: number | null; note?: string }

const VITALS0 = { respiratoryRate: "", spo2: "", onOxygen: false, systolicBp: "", pulse: "", temperature: "", consciousness: "alert", painScore: "", mobility: "", note: "" };

/** Ward nursing operations (Phase 4.4): the inpatient census as a
 *  worklist, the medication administration record generated from
 *  active prescriptions (recorded with a mandatory reason whenever
 *  a dose is not given), and observation rounds with NEWS2 computed
 *  on the server. */
export default function NursingPage() {
  const { lang } = useLang();
  const ar = lang === "ar";
  const [census, setCensus] = useState<CensusResp | null>(null);
  const [selected, setSelected] = useState<CensusRow | null>(null);
  const [mar, setMar] = useState<MarRow[]>([]);
  const [rounds, setRounds] = useState<Round[]>([]);
  const [vitals, setVitals] = useState(VITALS0);
  const [reasonFor, setReasonFor] = useState<{ dose: Dose; status: string } | null>(null);
  const [reason, setReason] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [toast, setToast] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  const showToast = (m: string) => { setToast(m); setTimeout(() => setToast(null), 3500); };

  const loadCensus = useCallback(async () => {
    try { setCensus(await api.getJson<CensusResp>("/api/nursing/census")); }
    catch (e) { setError(e instanceof Error ? e.message : String(e)); }
  }, []);
  useEffect(() => { loadCensus(); }, [loadCensus]);

  const loadPatient = useCallback(async (row: CensusRow) => {
    try {
      const [m, r] = await Promise.all([
        api.getJson<{ mar: MarRow[] }>(`/api/nursing/mar?admissionId=${row.admission.id}`),
        api.getJson<{ rounds: Round[] }>(`/api/nursing/rounds?admissionId=${row.admission.id}`),
      ]);
      setMar(m.mar); setRounds(r.rounds);
    } catch (e) { setError(e instanceof Error ? e.message : String(e)); }
  }, []);

  useEffect(() => { if (selected) loadPatient(selected); }, [selected, loadPatient]);

  async function generateMar() {
    if (!selected) return;
    setBusy(true);
    try {
      const r = await api.postJson<{ created: number }>("/api/nursing/mar/generate", { admissionId: selected.admission.id });
      showToast(ar ? `وُلّدت ${r.created} جرعة من الوصفات النشطة` : `Generated ${r.created} doses from active prescriptions`);
      await loadPatient(selected); await loadCensus();
    } catch (e) { setError(e instanceof Error ? e.message : String(e)); } finally { setBusy(false); }
  }

  async function recordDose(dose: Dose, status: string, rsn?: string) {
    try {
      await api.postJson(`/api/nursing/mar/${dose.id}/record`, { status, reason: rsn ?? undefined });
      showToast(ar ? "سُجّلت الجرعة" : "Dose recorded");
      setReasonFor(null); setReason("");
      if (selected) { await loadPatient(selected); await loadCensus(); }
    } catch (e) { setError(e instanceof Error ? e.message : String(e)); }
  }

  async function recordRound(e: React.FormEvent) {
    e.preventDefault();
    if (!selected) return;
    setBusy(true);
    try {
      await api.postJson("/api/nursing/rounds", {
        admissionId: selected.admission.id,
        respiratoryRate: vitals.respiratoryRate ? Number(vitals.respiratoryRate) : undefined,
        spo2: vitals.spo2 ? Number(vitals.spo2) : undefined,
        onOxygen: vitals.onOxygen,
        systolicBp: vitals.systolicBp ? Number(vitals.systolicBp) : undefined,
        pulse: vitals.pulse ? Number(vitals.pulse) : undefined,
        temperature: vitals.temperature ? Number(vitals.temperature) : undefined,
        consciousness: vitals.consciousness,
        painScore: vitals.painScore ? Number(vitals.painScore) : undefined,
        mobility: vitals.mobility || undefined,
        note: vitals.note || undefined,
      });
      showToast(ar ? "سُجّلت جولة الملاحظة" : "Round recorded");
      setVitals(VITALS0);
      await loadPatient(selected); await loadCensus();
    } catch (e) { setError(e instanceof Error ? e.message : String(e)); } finally { setBusy(false); }
  }

  const stateBadge = (s: string) =>
    s === "overdue" ? <span className="badge danger">{ar ? "متأخرة" : "Overdue"}</span>
    : s === "due" ? <span className="badge warn">{ar ? "مستحقة الآن" : "Due"}</span>
    : s === "given" ? <span className="badge ok">{ar ? "أُعطيت" : "Given"}</span>
    : s === "upcoming" ? <span className="badge">{ar ? "قادمة" : "Upcoming"}</span>
    : <span className="badge">{s}</span>;

  return (
    <>
      <PageHead
        eyebrow={ar ? "التمريض" : "Nursing"}
        title={ar ? "التمريض — تعداد الأجنحة وسجل إعطاء الأدوية والجولات" : "Nursing — Ward Census, MAR & Rounds"}
        sub={ar
          ? "قائمة عمل التمريض للمقيمين حاليًا: جرعات اليوم مولّدة من الوصفات النشطة، ولا يُسجَّل عدم إعطاء أي جرعة إلا بسبب موثق، وجولات ملاحظة بدرجة NEWS2 محسوبة خادميًا."
          : "The nursing worklist for current inpatients: today's doses generated from active prescriptions, no dose recorded as not-given without a documented reason, and observation rounds with server-computed NEWS2."}
      />
      {error && <div className="alert-error">{error} <Button variant="soft" small onClick={() => setError(null)}>{ar ? "إغلاق" : "Dismiss"}</Button></div>}
      <Toasts items={toast ? [{ text: toast }] : []} />

      <div className="kpi-grid">
        <KpiCard label={ar ? "مقيمون حاليًا" : "Current inpatients"} value={census?.count ?? "—"} />
        <KpiCard label={ar ? "جرعات مستحقة الآن" : "Doses due now"} value={census?.dueDoses ?? "—"} tone="warn" />
        <KpiCard label={ar ? "جرعات متأخرة" : "Overdue doses"} value={census?.overdueDoses ?? "—"} tone="danger" />
      </div>

      <div className="card">
        <h2>{ar ? "تعداد الأجنحة" : "Ward census"}</h2>
        {!census ? <p className="muted">{ar ? "جارٍ التحميل…" : "Loading…"}</p> : census.census.length === 0 ? (
          <div className="empty">{ar ? "لا مقيمين حاليًا — التنويم من شاشة القبول والأسرّة." : "No current inpatients — admit from the ADT & beds screen."}</div>
        ) : (
          <div className="tbl-wrap">
            <table className="tbl">
              <thead><tr><th>{ar ? "المريض" : "Patient"}</th><th>{ar ? "الجناح / السرير" : "Ward / Bed"}</th><th>{ar ? "جرعات مستحقة" : "Due"}</th><th>{ar ? "متأخرة" : "Overdue"}</th><th>{ar ? "مهام مفتوحة" : "Open tasks"}</th><th>{ar ? "آخر NEWS2" : "Last NEWS2"}</th><th></th></tr></thead>
              <tbody>
                {census.census.map((r) => (
                  <tr key={r.admission.id}>
                    <td>{r.patientName}</td>
                    <td>{r.wardName} · {r.admission.bedLabel}</td>
                    <td>{r.dueDoses > 0 ? <span className="badge warn">{r.dueDoses}</span> : 0}</td>
                    <td>{r.overdueDoses > 0 ? <span className="badge danger">{r.overdueDoses}</span> : 0}</td>
                    <td>{r.openTasks}</td>
                    <td>{r.lastNews2 != null ? <span className="badge info">{r.lastNews2}</span> : "—"}</td>
                    <td><Button small onClick={() => setSelected(r)}>{ar ? "ملف التمريض" : "Nursing file"}</Button></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {selected && (
        <div className="card">
          <h2>{selected.patientName} — {selected.wardName} · {ar ? "سرير" : "Bed"} {selected.admission.bedLabel}</h2>
          <h3>{ar ? "سجل إعطاء الأدوية — اليوم" : "Medication administration — today"}</h3>
          <Button small disabled={busy} onClick={generateMar}>{ar ? "توليد جرعات اليوم من الوصفات" : "Generate today's doses from prescriptions"}</Button>
          {mar.length === 0 ? <p className="muted mt">{ar ? "لا جرعات مجدولة اليوم — ولّدها من الوصفات النشطة أو راجع وصفات المريض." : "No doses scheduled today — generate them from active prescriptions."}</p> : (
            <div className="tbl-wrap mt">
              <table className="tbl">
                <thead><tr><th>{ar ? "الدواء" : "Drug"}</th><th>{ar ? "الجرعة" : "Dose"}</th><th>{ar ? "الموعد" : "Scheduled"}</th><th>{ar ? "الحالة" : "State"}</th><th></th></tr></thead>
                <tbody>
                  {mar.map((row) => (
                    <tr key={row.administration.id}>
                      <td>{row.administration.drugName}</td>
                      <td>{row.administration.dose ?? "—"}</td>
                      <td dir="ltr">{new Date(row.administration.scheduledAt).toLocaleTimeString(ar ? "ar-EG" : "en-GB", { hour: "2-digit", minute: "2-digit" })}</td>
                      <td>{stateBadge(row.administration.status === "scheduled" ? row.state : row.administration.status)}{row.administration.reason ? <span className="muted"> · {row.administration.reason}</span> : null}</td>
                      <td>
                        {row.administration.status === "scheduled" && (
                          <span className="row">
                            <Button small onClick={() => recordDose(row.administration, "given")}>{ar ? "أُعطيت" : "Given"}</Button>
                            <Button small variant="soft" onClick={() => setReasonFor({ dose: row.administration, status: "refused" })}>{ar ? "رُفضت" : "Refused"}</Button>
                            <Button small variant="soft" onClick={() => setReasonFor({ dose: row.administration, status: "held" })}>{ar ? "مُعلّقة" : "Held"}</Button>
                            <Button small variant="soft" onClick={() => setReasonFor({ dose: row.administration, status: "missed" })}>{ar ? "فائتة" : "Missed"}</Button>
                          </span>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
          {reasonFor && (
            <div className="divider-top">
              <div className="field">
                <label>{ar ? "سبب عدم الإعطاء (إلزامي وموثق)" : "Reason for not giving (mandatory, documented)"}</label>
                <div className="row">
                  <input className="grow" value={reason} onChange={(e) => setReason(e.target.value)} placeholder={ar ? "مثال: المريض صائم قبل عملية" : "e.g. patient fasting pre-op"} />
                  <Button small disabled={!reason.trim()} onClick={() => recordDose(reasonFor.dose, reasonFor.status, reason)}>{ar ? "تسجيل" : "Record"}</Button>
                  <Button small variant="soft" onClick={() => setReasonFor(null)}>{ar ? "إلغاء" : "Cancel"}</Button>
                </div>
              </div>
            </div>
          )}

          <h3>{ar ? "جولة ملاحظة جديدة" : "New observation round"}</h3>
          <form onSubmit={recordRound}>
            <div className="form-grid">
              <div className="field"><label>{ar ? "معدل التنفس" : "Respiratory rate"}</label><input inputMode="numeric" value={vitals.respiratoryRate} onChange={(e) => setVitals({ ...vitals, respiratoryRate: e.target.value })} /></div>
              <div className="field"><label>{ar ? "تشبع الأكسجين %" : "SpO2 %"}</label><input inputMode="numeric" value={vitals.spo2} onChange={(e) => setVitals({ ...vitals, spo2: e.target.value })} /></div>
              <div className="field"><label>{ar ? "الضغط الانقباضي" : "Systolic BP"}</label><input inputMode="numeric" value={vitals.systolicBp} onChange={(e) => setVitals({ ...vitals, systolicBp: e.target.value })} /></div>
              <div className="field"><label>{ar ? "النبض" : "Pulse"}</label><input inputMode="numeric" value={vitals.pulse} onChange={(e) => setVitals({ ...vitals, pulse: e.target.value })} /></div>
              <div className="field"><label>{ar ? "الحرارة" : "Temperature"}</label><input inputMode="decimal" value={vitals.temperature} onChange={(e) => setVitals({ ...vitals, temperature: e.target.value })} /></div>
              <div className="field"><label>{ar ? "الألم (0-10)" : "Pain (0-10)"}</label><input inputMode="numeric" value={vitals.painScore} onChange={(e) => setVitals({ ...vitals, painScore: e.target.value })} /></div>
              <div className="field"><label>{ar ? "الوعي" : "Consciousness"}</label>
                <select value={vitals.consciousness} onChange={(e) => setVitals({ ...vitals, consciousness: e.target.value })}>
                  <option value="alert">{ar ? "يقظ" : "Alert"}</option>
                  <option value="voice">{ar ? "يستجيب للصوت" : "Voice"}</option>
                  <option value="pain">{ar ? "يستجيب للألم" : "Pain"}</option>
                  <option value="unresponsive">{ar ? "لا يستجيب" : "Unresponsive"}</option>
                </select></div>
              <div className="field"><label>{ar ? "على أكسجين" : "On oxygen"}</label>
                <span className="inline-check"><input type="checkbox" checked={vitals.onOxygen} onChange={(e) => setVitals({ ...vitals, onOxygen: e.target.checked })} /> {ar ? "نعم" : "Yes"}</span></div>
              <div className="field"><label>{ar ? "الحركة" : "Mobility"}</label><input value={vitals.mobility} onChange={(e) => setVitals({ ...vitals, mobility: e.target.value })} placeholder={ar ? "مستقل / بمساعدة / ملازم للفراش" : "independent / assisted / bedbound"} /></div>
              <div className="field"><label>{ar ? "ملاحظة" : "Note"}</label><input value={vitals.note} onChange={(e) => setVitals({ ...vitals, note: e.target.value })} /></div>
            </div>
            <div className="form-actions">
              <button type="submit" className="btn" disabled={busy}>{ar ? "تسجيل الجولة" : "Record round"}</button>
            </div>
          </form>

          {rounds.length > 0 && (
            <>
              <h3>{ar ? "الجولات السابقة" : "Previous rounds"}</h3>
              <div className="tbl-wrap">
                <table className="tbl">
                  <thead><tr><th>{ar ? "الوقت" : "Time"}</th><th>NEWS2</th><th>{ar ? "الألم" : "Pain"}</th><th>SpO2</th><th>{ar ? "النبض" : "Pulse"}</th><th>{ar ? "ملاحظة" : "Note"}</th></tr></thead>
                  <tbody>
                    {rounds.map((r) => (
                      <tr key={r.id}>
                        <td dir="ltr">{new Date(r.roundAt).toLocaleString(ar ? "ar-EG" : "en-GB")}</td>
                        <td>{r.news2Total != null ? <span className="badge info">{r.news2Total} · {r.news2Band}</span> : "—"}</td>
                        <td>{r.painScore ?? "—"}</td>
                        <td>{r.spo2 ?? "—"}</td>
                        <td>{r.pulse ?? "—"}</td>
                        <td>{r.note ?? "—"}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </>
          )}
        </div>
      )}
    </>
  );
}
