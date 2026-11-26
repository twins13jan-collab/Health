"use client";

import { useCallback, useEffect, useState } from "react";
import KpiCard from "@/components/KpiCard";
import { Button, Drawer, PageHead, Toasts } from "@/components/ui";
import { api } from "@/lib/api";
import { useLang } from "@/lib/i18n";

interface Vital { value: number; quality: string; source_time: string; freshness: string; present_as_live: boolean }
interface Infusion { id: string; drug_code: string; dose_rate: number; rate_unit: string }
interface Escalation { id: string; reason: string; team_called: string; called_at: string }
interface Stay { id: string; patientId: string; admissionReason: string; careLevel: string; team?: string; primaryDiagnosis?: string; referenceWeightKg?: number; weightVerified: boolean; admittedAt: string; status: string }
interface BoardRow { stay: Stay; patient_name: string; signal: string; vitals: Record<string, Vital>; ventilated: boolean; vent_id: string; vent_mode: string; running_infusions: Infusion[]; open_escalations: number; latest_escalation_reason: string; open_escalation_list: Escalation[] }
interface BoardResp { board: BoardRow[]; count: number; summary: { active_stays: number; ventilated: number; signal_lost: number; stale: number; no_data: number; open_escalations: number; weight_unverified: number } }

const KINDS: { key: string; ar: string; en: string; unit: string }[] = [
  { key: "heart_rate", ar: "النبض", en: "Heart rate", unit: "/د" },
  { key: "spo2", ar: "الأكسجين", en: "SpO₂", unit: "%" },
  { key: "respiratory_rate", ar: "التنفس", en: "Resp. rate", unit: "/د" },
  { key: "systolic_bp", ar: "الضغط الانقباضي", en: "Systolic BP", unit: "mmHg" },
  { key: "map", ar: "الضغط الوسطي", en: "MAP", unit: "mmHg" },
  { key: "temperature", ar: "الحرارة", en: "Temp", unit: "°C" },
  { key: "gcs", ar: "غلاسكو", en: "GCS", unit: "" },
  { key: "urine_output", ar: "إخراج البول", en: "Urine out", unit: "ml" },
];
const SIGNAL: Record<string, { ar: string; en: string; tone: string }> = {
  live: { ar: "إشارة حية", en: "Live signal", tone: "ok" },
  stale: { ar: "بيانات قديمة", en: "Stale data", tone: "warn" },
  signal_lost: { ar: "الإشارة مفقودة", en: "Signal lost", tone: "danger" },
  no_data: { ar: "بلا بيانات بعد", en: "No data yet", tone: "info" },
};

/** ICU operational board (Phase 4.6, cycle 221): active stays as an
 *  attention queue. Device vitals carry their freshness — stale data is
 *  labelled and signal-lost stays are flagged, never shown as live. */
export default function IcuPage() {
  const { lang, t } = useLang();
  const ar = lang === "ar";
  const [board, setBoard] = useState<BoardResp | null>(null);
  const [selected, setSelected] = useState<BoardRow | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [toasts, setToasts] = useState<{ text: string; err?: boolean }[]>([]);
  const [busy, setBusy] = useState(false);
  const [obs, setObs] = useState({ kind: "heart_rate", value: "", unit: "", deviceCode: "", sourceTime: "" });
  const [vent, setVent] = useState({ deviceCode: "", mode: "SIMV", fio2: "" });
  const [inf, setInf] = useState({ drugCode: "", doseRate: "", rateUnit: "mcg/kg/min" });
  const [esc, setEsc] = useState({ reason: "", teamCalled: "" });
  const [decision, setDecision] = useState("");
  const [endStatus, setEndStatus] = useState("transferred");

  const toast = (text: string, err?: boolean) => { setToasts((x) => [...x, { text, err }]); setTimeout(() => setToasts((x) => x.slice(1)), 4000); };

  const load = useCallback(async () => {
    try {
      const b = await api.getJson<BoardResp>("/api/icu/board");
      setBoard(b);
      setSelected((sel) => (sel ? b.board.find((r) => r.stay.id === sel.stay.id) ?? null : null));
    } catch (e) { setError(e instanceof Error ? e.message : String(e)); }
  }, []);
  useEffect(() => { load(); const iv = setInterval(load, 30000); return () => clearInterval(iv); }, [load]);

  async function act(fn: () => Promise<unknown>, okMsg: string) {
    setBusy(true);
    try { await fn(); toast(okMsg); await load(); }
    catch (e) { toast(e instanceof Error ? e.message : String(e), true); }
    finally { setBusy(false); }
  }

  const s = board?.summary;

  return (
    <div>
      <PageHead eyebrow={ar ? "التشغيل السريري" : "Clinical operations"} title={t("icu")}
        sub={ar ? "لوحة الوحدة: الإقامات النشطة كطابور انتباه — التصعيدات المفتوحة أولًا، ثم الإشارة المفقودة. بيانات الأجهزة المتقادمة تُوسم ولا تُعرض كحية أبدًا."
               : "Unit board: active stays as an attention queue — open escalations first, then lost signals. Stale device data is labelled, never shown live."}
        actions={<Button onClick={load}>{ar ? "تحديث" : "Refresh"}</Button>} />
      {error ? <div className="alert-error">{error}</div> : null}
      <div className="kpi-grid">
        <KpiCard label={ar ? "إقامات نشطة" : "Active stays"} value={s?.active_stays ?? "—"} />
        <KpiCard label={ar ? "على تنفس صناعي" : "Ventilated"} value={s?.ventilated ?? "—"} />
        <KpiCard label={ar ? "إشارة مفقودة" : "Signal lost"} value={s?.signal_lost ?? "—"} />
        <KpiCard label={ar ? "تصعيدات مفتوحة" : "Open escalations"} value={s?.open_escalations ?? "—"} />
        <KpiCard label={ar ? "وزن غير موثّق" : "Weight unverified"} value={s?.weight_unverified ?? "—"} />
      </div>

      {!board ? <div className="empty">{ar ? "جارٍ التحميل…" : "Loading…"}</div>
        : board.board.length === 0 ? <div className="empty">{ar ? "لا إقامات عناية نشطة في منشأتك الآن" : "No active ICU stays in your facility"}</div>
        : (
          <div className="cards-grid">
            {board.board.map((row) => {
              const sig = SIGNAL[row.signal] ?? SIGNAL.no_data;
              return (
                <section key={row.stay.id} className="card">
                  <div className="l-row">
                    <strong>{row.patient_name || row.stay.patientId}</strong>
                    <span className={`badge ${sig.tone}`}>{ar ? sig.ar : sig.en}</span>
                    <span className="badge info">{row.stay.careLevel}</span>
                    {row.open_escalations > 0 ? <span className="badge danger">{ar ? "تصعيد مفتوح" : "Escalation open"} · {row.open_escalations}</span> : null}
                  </div>
                  <div className="small muted">{row.stay.admissionReason}{row.stay.primaryDiagnosis ? ` — ${row.stay.primaryDiagnosis}` : ""}</div>
                  <div className="l-row">
                    {KINDS.filter((k) => row.vitals[k.key]).map((k) => {
                      const v = row.vitals[k.key];
                      const tone = v.freshness === "LIVE" ? "ok" : v.freshness === "STALE" ? "warn" : "danger";
                      return <span key={k.key} className={`badge ${tone}`}>{ar ? k.ar : k.en}: {v.value}{k.unit}{v.present_as_live ? "" : ar ? " (غير حية)" : " (not live)"}</span>;
                    })}
                    {Object.keys(row.vitals).length === 0 ? <span className="small muted">{ar ? "لا قراءات أجهزة بعد" : "No device readings yet"}</span> : null}
                  </div>
                  <div className="l-row small">
                    {row.ventilated ? <span className="badge warn">{ar ? "تنفس صناعي" : "Ventilated"} · {row.vent_mode}</span> : null}
                    {row.running_infusions.map((i) => <span key={i.id} className="badge info">{i.drug_code} {i.dose_rate} {i.rate_unit}</span>)}
                    {!row.stay.weightVerified ? <span className="badge warn">{ar ? "الوزن المرجعي غير موثّق — حساب الجرعة بالوزن محظور" : "Reference weight unverified — weight dosing blocked"}</span> : null}
                  </div>
                  <div className="l-actions">
                    <Button onClick={() => setSelected(row)}>{ar ? "إدارة الحالة" : "Manage stay"}</Button>
                  </div>
                </section>
              );
            })}
          </div>
        )}

      {selected ? (
        <Drawer title={selected.patient_name || selected.stay.patientId} onClose={() => setSelected(null)}>
          <div className="form-grid">
            <div className="field"><label>{ar ? "نوع القراءة" : "Reading kind"}</label>
              <select value={obs.kind} onChange={(e) => setObs({ ...obs, kind: e.target.value })}>
                {KINDS.map((k) => <option key={k.key} value={k.key}>{ar ? k.ar : k.en}</option>)}
              </select></div>
            <div className="field"><label>{ar ? "القيمة" : "Value"}</label><input type="number" step="0.1" value={obs.value} onChange={(e) => setObs({ ...obs, value: e.target.value })} /></div>
            <div className="field"><label>{ar ? "كود الجهاز" : "Device code"}</label><input value={obs.deviceCode} onChange={(e) => setObs({ ...obs, deviceCode: e.target.value })} /></div>
            <div className="field"><label>{ar ? "وقت المصدر" : "Source time"}</label><input type="datetime-local" value={obs.sourceTime} onChange={(e) => setObs({ ...obs, sourceTime: e.target.value })} /></div>
          </div>
          <div className="form-actions">
            <Button disabled={busy || !obs.value} onClick={() => act(() => api.postJson(`/api/icu/stays/${selected.stay.id}/observations`, {
              kind: obs.kind, value: Number(obs.value), unit: obs.unit || undefined, deviceCode: obs.deviceCode || undefined,
              sourceTime: obs.sourceTime ? new Date(obs.sourceTime).toISOString() : new Date().toISOString(),
            }), ar ? "سُجلت القراءة" : "Reading recorded")}>{ar ? "تسجيل قراءة" : "Record reading"}</Button>
          </div>

          <h4>{ar ? "التنفس الصناعي" : "Ventilation"}</h4>
          {selected.ventilated ? (
            <div className="form-actions"><span className="badge warn">{selected.vent_mode}</span>
              <Button disabled={busy} onClick={() => act(() => api.postJson(`/api/icu/ventilation/${selected.vent_id}/end`, {}), ar ? "أُنهيت حلقة التنفس" : "Ventilation ended")}>{ar ? "إنهاء التنفس" : "End ventilation"}</Button></div>
          ) : (
            <div className="form-grid">
              <div className="field"><label>{ar ? "الجهاز" : "Device"}</label><input value={vent.deviceCode} onChange={(e) => setVent({ ...vent, deviceCode: e.target.value })} /></div>
              <div className="field"><label>{ar ? "النمط" : "Mode"}</label><input value={vent.mode} onChange={(e) => setVent({ ...vent, mode: e.target.value })} /></div>
              <div className="field"><label>FiO₂</label><input type="number" step="0.01" value={vent.fio2} onChange={(e) => setVent({ ...vent, fio2: e.target.value })} /></div>
              <div className="form-actions"><Button disabled={busy || !vent.deviceCode} onClick={() => act(() => api.postJson(`/api/icu/stays/${selected.stay.id}/ventilation`, {
                deviceCode: vent.deviceCode, mode: vent.mode, fio2: vent.fio2 ? Number(vent.fio2) : undefined,
              }), ar ? "بدأ التنفس الصناعي" : "Ventilation started")}>{ar ? "بدء التنفس" : "Start ventilation"}</Button></div>
            </div>
          )}

          <h4>{ar ? "التسريبات الوريدية" : "Infusions"}</h4>
          {selected.running_infusions.map((i) => (
            <div key={i.id} className="l-row"><span className="badge info">{i.drug_code} · {i.dose_rate} {i.rate_unit}</span>
              <Button disabled={busy} onClick={() => act(() => api.postJson(`/api/icu/infusions/${i.id}/stop`, { status: "stopped" }), ar ? "أُوقف التسريب" : "Infusion stopped")}>{ar ? "إيقاف" : "Stop"}</Button></div>
          ))}
          <div className="form-grid">
            <div className="field"><label>{ar ? "كود الدواء" : "Drug code"}</label><input value={inf.drugCode} onChange={(e) => setInf({ ...inf, drugCode: e.target.value })} /></div>
            <div className="field"><label>{ar ? "المعدل" : "Rate"}</label><input type="number" step="0.01" value={inf.doseRate} onChange={(e) => setInf({ ...inf, doseRate: e.target.value })} /></div>
            <div className="field"><label>{ar ? "وحدة المعدل" : "Rate unit"}</label>
              <select value={inf.rateUnit} onChange={(e) => setInf({ ...inf, rateUnit: e.target.value })}>
                <option value="mcg/kg/min">mcg/kg/min</option><option value="mg/hr">mg/hr</option><option value="ml/hr">ml/hr</option><option value="units/hr">units/hr</option>
              </select></div>
            <div className="form-actions"><Button disabled={busy || !inf.drugCode || !inf.doseRate} onClick={() => act(() => api.postJson(`/api/icu/stays/${selected.stay.id}/infusions`, {
              drugCode: inf.drugCode, doseRate: Number(inf.doseRate), rateUnit: inf.rateUnit,
            }), ar ? "بدأ التسريب" : "Infusion started")}>{ar ? "بدء تسريب" : "Start infusion"}</Button></div>
          </div>

          <h4>{ar ? "التصعيد" : "Escalation"}</h4>
          {selected.open_escalation_list.map((e) => (
            <div key={e.id} className="l-row"><span className="badge danger">{e.reason} · {e.team_called}</span></div>
          ))}
          {selected.open_escalation_list.length > 0 ? (
            <div className="form-grid">
              <div className="field"><label>{ar ? "قرار الفريق المستدعى" : "Team decision"}</label><input value={decision} onChange={(e) => setDecision(e.target.value)} /></div>
              <div className="form-actions"><Button disabled={busy || !decision} onClick={() => act(() => api.postJson(`/api/icu/escalations/${selected.open_escalation_list[0].id}/respond`, { decision }), ar ? "سُجلت الاستجابة" : "Response recorded")}>{ar ? "تسجيل الاستجابة" : "Record response"}</Button></div>
            </div>
          ) : null}
          <div className="form-grid">
            <div className="field"><label>{ar ? "سبب التصعيد" : "Reason"}</label><input value={esc.reason} onChange={(e) => setEsc({ ...esc, reason: e.target.value })} /></div>
            <div className="field"><label>{ar ? "الفريق المستدعى" : "Team called"}</label><input value={esc.teamCalled} onChange={(e) => setEsc({ ...esc, teamCalled: e.target.value })} /></div>
            <div className="form-actions"><Button disabled={busy || !esc.reason || !esc.teamCalled} onClick={() => act(() => api.postJson(`/api/icu/stays/${selected.stay.id}/escalations`, esc), ar ? "فُتح التصعيد" : "Escalation called")}>{ar ? "استدعاء فريق" : "Call team"}</Button></div>
          </div>

          <h4>{ar ? "إنهاء الإقامة" : "End stay"}</h4>
          <div className="form-grid">
            <div className="field"><label>{ar ? "الحالة" : "Outcome"}</label>
              <select value={endStatus} onChange={(e) => setEndStatus(e.target.value)}>
                <option value="transferred">{ar ? "تحويل لمستوى رعاية آخر" : "Transferred"}</option>
                <option value="discharged">{ar ? "خروج" : "Discharged"}</option>
                <option value="deceased">{ar ? "وفاة" : "Deceased"}</option>
              </select></div>
            <div className="form-actions"><Button disabled={busy} onClick={() => act(async () => { await api.postJson(`/api/icu/stays/${selected.stay.id}/status`, { status: endStatus }); setSelected(null); }, ar ? "أُنهيت الإقامة" : "Stay ended")}>{ar ? "إنهاء" : "End stay"}</Button></div>
          </div>
        </Drawer>
      ) : null}
      <Toasts items={toasts} />
    </div>
  );
}
