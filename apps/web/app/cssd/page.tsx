"use client";

import { useCallback, useEffect, useState } from "react";
import KpiCard from "@/components/KpiCard";
import { Button, Drawer, PageHead, SectionHead, Toasts } from "@/components/ui";
import { api } from "@/lib/api";
import { useLang } from "@/lib/i18n";

interface SetRow { set_id: string; set_code: string; name?: string; status: string; contains_implants: boolean; storage_location?: string; expiry_date?: string; expiry_state: string; cycle_id?: string; cycle_status?: string; missing_steps?: string[]; chemical_indicator?: string; biological_indicator?: string; releasable?: boolean }
interface StRow { sterilizer_code: string; status: string; maintenance_due?: string; maintenance_state: string; calibration_due?: string; calibration_state: string; attention: boolean }
interface BoardResp { sets: SetRow[]; sterilizers: StRow[]; summary: { in_process: number; awaiting_release: number; quarantined: number; recalled: number; sterile_available: number; expiring_soon: number; expired_in_storage: number; open_failures: number; sterilizers_attention: number; released_today: number; issued_today: number } }

const STATUS_TONE: Record<string, string> = { recalled: "danger", quarantined: "danger", sterile: "ok", in_process: "warn", issued: "info", in_use: "info" };
const STEP_LABEL: Record<string, { ar: string; en: string }> = {
  cleaning: { ar: "تنظيف", en: "Cleaning" }, disinfection: { ar: "تطهير", en: "Disinfection" },
  drying: { ar: "تجفيف", en: "Drying" }, inspection: { ar: "فحص", en: "Inspection" }, packing: { ar: "تغليف", en: "Packing" },
};
const DUE_TONE: Record<string, string> = { overdue: "danger", due_soon: "warn", ok: "ok", unknown: "info" };

/** CSSD department board (Phase 4.10, module 105): every instrument
 *  set as an attention row — recalls and quarantines first, expired
 *  sterile stock next, then expiring stock and work in process with
 *  its missing chain steps named. Release readiness is computed by
 *  the same rules the release endpoint enforces; sterilizers show
 *  their maintenance and calibration due states. */
export default function CssdPage() {
  const { lang, t } = useLang();
  const ar = lang === "ar";
  const [board, setBoard] = useState<BoardResp | null>(null);
  const [selected, setSelected] = useState<SetRow | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [toasts, setToasts] = useState<{ text: string; err?: boolean }[]>([]);
  const [busy, setBusy] = useState(false);
  const [steps, setSteps] = useState({ cleaning: false, disinfection: false, dried: false, inspected: false, packedBy: "", sterilizerCode: "", cycleNo: "" });
  const [indicators, setIndicators] = useState({ chemical: "pass", biological: "" });
  const [release, setRelease] = useState({ storageLocation: "", expiryDate: "" });
  const [use, setUse] = useState({ procedureRef: "", patientRef: "" });
  const [newSet, setNewSet] = useState<{ open: boolean; setCode: string; name: string; containsImplants: boolean }>({ open: false, setCode: "", name: "", containsImplants: false });
  const [receiveFor, setReceiveFor] = useState<SetRow | null>(null);
  const [receive, setReceive] = useState({ receivedFrom: "", sortedBy: "" });

  const toast = (text: string, err?: boolean) => { setToasts((x) => [...x, { text, err }]); setTimeout(() => setToasts((x) => x.slice(1)), 4500); };

  const load = useCallback(async () => {
    try {
      const b = await api.getJson<BoardResp>("/api/cssd/board");
      setBoard(b);
      setSelected((sel) => (sel ? b.sets.find((r) => r.set_id === sel.set_id) ?? null : null));
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
  const sel = selected;
  const stepLabel = (x: string) => (ar ? STEP_LABEL[x]?.ar ?? x : STEP_LABEL[x]?.en ?? x);

  return (
    <div>
      <PageHead eyebrow={ar ? "التشغيل السريري" : "Clinical operations"} title={t("cssd")}
        sub={ar ? "لوحة قسم التعقيم: المستدعى والمحجوز أولًا، ثم المخزون المعقم المنتهي أو القريب من الانتهاء، ثم المعالجة الجارية بخطواتها الناقصة مسماة. لا إفراج قبل سلسلة مكتملة ومؤشر كيميائي ناجح — وأحمال الغرسات تنتظر المؤشر البيولوجي — وطقم منتهٍ لا يُصرف أبدًا."
               : "The CSSD board: recalls and quarantines first, expired and expiring sterile stock next, then work in process with missing steps named. No release before a complete chain and a passed chemical indicator — implant loads wait for the biological — and an expired set is never issued."}
        actions={<>
          <Button onClick={() => setNewSet({ open: true, setCode: "", name: "", containsImplants: false })}>{ar ? "تسجيل طقم" : "Register set"}</Button>
          <Button onClick={load}>{ar ? "تحديث" : "Refresh"}</Button>
        </>} />
      {error ? <div className="alert-error">{error}</div> : null}
      <div className="kpi-grid">
        <KpiCard label={ar ? "قيد المعالجة" : "In process"} value={s?.in_process ?? "—"} />
        <KpiCard label={ar ? "جاهز للإفراج" : "Awaiting release"} value={s?.awaiting_release ?? "—"} />
        <KpiCard label={ar ? "محجوز" : "Quarantined"} value={s?.quarantined ?? "—"} />
        <KpiCard label={ar ? "مستدعى" : "Recalled"} value={s?.recalled ?? "—"} />
        <KpiCard label={ar ? "معقم متاح" : "Sterile available"} value={s?.sterile_available ?? "—"} />
        <KpiCard label={ar ? "ينتهي قريبًا / منتهٍ" : "Expiring / expired"} value={s ? `${s.expiring_soon} / ${s.expired_in_storage}` : "—"} />
      </div>

      {!board ? <div className="empty">{ar ? "جارٍ التحميل…" : "Loading…"}</div> : (
        <>
          {board.sterilizers.length > 0 ? (
            <>
              <SectionHead eyebrow={ar ? "الأجهزة" : "Equipment"} title={ar ? "أجهزة التعقيم — الصيانة والمعايرة" : "Sterilizers — maintenance & calibration"} />
              <div className="cards-grid">
                {board.sterilizers.map((st) => (
                  <section key={st.sterilizer_code} className="card">
                    <div className="l-row">
                      <strong>{st.sterilizer_code}</strong>
                      {st.attention ? <span className="badge danger">{ar ? "متأخر" : "Overdue"}</span> : null}
                    </div>
                    <div className="l-row">
                      <span className={`badge ${DUE_TONE[st.maintenance_state] ?? "info"}`}>{ar ? "صيانة" : "Maintenance"}: {st.maintenance_due ?? "—"}</span>
                      <span className={`badge ${DUE_TONE[st.calibration_state] ?? "info"}`}>{ar ? "معايرة" : "Calibration"}: {st.calibration_due ?? "—"}</span>
                    </div>
                  </section>
                ))}
              </div>
            </>
          ) : null}

          <SectionHead eyebrow={ar ? "الأطقم" : "Sets"} title={ar ? "أطقم الأدوات" : "Instrument sets"} />
          {board.sets.length === 0 ? <div className="empty">{ar ? "لا أطقم مسجلة في منشأتك" : "No sets registered in your facility"}</div> : (
            <div className="cards-grid">
              {board.sets.map((row) => (
                <section key={row.set_id} className="card">
                  <div className="l-row">
                    <strong>{row.set_code}</strong>
                    {row.name ? <span>{row.name}</span> : null}
                    <span className={`badge ${STATUS_TONE[row.status] ?? "info"}`}>{row.status}</span>
                    {row.contains_implants ? <span className="badge warn">{ar ? "يحتوي غرسات" : "Implants"}</span> : null}
                  </div>
                  <div className="l-row">
                    {row.expiry_state === "expired" ? <span className="badge danger">{ar ? "منتهي الصلاحية وهو على الرف" : "Expired in storage"} · {row.expiry_date}</span> : null}
                    {row.expiry_state === "expiring_soon" ? <span className="badge warn">{ar ? "ينتهي قريبًا" : "Expiring soon"} · {row.expiry_date}</span> : null}
                    {row.expiry_state === "valid" ? <span className="badge ok">{ar ? "صالح حتى" : "Valid to"} {row.expiry_date}</span> : null}
                    {row.storage_location ? <span className="badge info">{row.storage_location}</span> : null}
                    {row.releasable ? <span className="badge ok">{ar ? "جاهز للإفراج" : "Releasable"}</span> : null}
                  </div>
                  {row.missing_steps && row.missing_steps.length > 0 ? (
                    <div className="l-row">
                      <span className="small">{ar ? "خطوات ناقصة:" : "Missing steps:"}</span>
                      {row.missing_steps.map((m) => <span key={m} className="badge warn">{stepLabel(m)}</span>)}
                    </div>
                  ) : null}
                  {row.cycle_id ? (
                    <div className="l-row small">
                      {row.chemical_indicator ? <span className={`badge ${row.chemical_indicator === "pass" ? "ok" : row.chemical_indicator === "fail" ? "danger" : "warn"}`}>{ar ? "كيميائي" : "Chemical"}: {row.chemical_indicator}</span> : null}
                      {row.biological_indicator ? <span className={`badge ${row.biological_indicator === "pass" ? "ok" : row.biological_indicator === "fail" ? "danger" : "warn"}`}>{ar ? "بيولوجي" : "Biological"}: {row.biological_indicator}</span> : null}
                    </div>
                  ) : null}
                  <div className="l-actions">
                    {row.status === "in_use" || row.status === "issued" ? (
                      <Button disabled={busy} onClick={() => { setReceiveFor(row); setReceive({ receivedFrom: "", sortedBy: "" }); }}>{ar ? "استلام لإعادة المعالجة" : "Receive for reprocessing"}</Button>
                    ) : null}
                    {row.cycle_id && row.cycle_status === "in_progress" ? (
                      <Button onClick={() => setSelected(row)}>{ar ? "إدارة الدورة" : "Manage cycle"}</Button>
                    ) : null}
                    {row.status === "sterile" ? (
                      <Button onClick={() => { setSelected(row); setUse({ procedureRef: "", patientRef: "" }); }}>{ar ? "صرف للاستخدام" : "Issue for use"}</Button>
                    ) : null}
                  </div>
                </section>
              ))}
            </div>
          )}
        </>
      )}

      {sel && sel.cycle_id && sel.cycle_status === "in_progress" ? (
        <Drawer title={`${sel.set_code} — ${ar ? "دورة إعادة المعالجة" : "Reprocessing cycle"}`} onClose={() => setSelected(null)}>
          <h4>{ar ? "خطوات السلسلة" : "Chain steps"}</h4>
          <div className="form-grid">
            <div className="field"><label>{ar ? "تنظيف" : "Cleaning"}</label><input type="checkbox" checked={steps.cleaning} onChange={(e) => setSteps({ ...steps, cleaning: e.target.checked })} /></div>
            <div className="field"><label>{ar ? "تطهير" : "Disinfection"}</label><input type="checkbox" checked={steps.disinfection} onChange={(e) => setSteps({ ...steps, disinfection: e.target.checked })} /></div>
            <div className="field"><label>{ar ? "تجفيف" : "Dried"}</label><input type="checkbox" checked={steps.dried} onChange={(e) => setSteps({ ...steps, dried: e.target.checked })} /></div>
            <div className="field"><label>{ar ? "فحص" : "Inspected"}</label><input type="checkbox" checked={steps.inspected} onChange={(e) => setSteps({ ...steps, inspected: e.target.checked })} /></div>
            <div className="field"><label>{ar ? "غلّفها" : "Packed by"}</label><input value={steps.packedBy} onChange={(e) => setSteps({ ...steps, packedBy: e.target.value })} /></div>
            <div className="field"><label>{ar ? "جهاز التعقيم" : "Sterilizer"}</label><input value={steps.sterilizerCode} onChange={(e) => setSteps({ ...steps, sterilizerCode: e.target.value })} /></div>
            <div className="field"><label>{ar ? "رقم الدورة" : "Cycle no."}</label><input value={steps.cycleNo} onChange={(e) => setSteps({ ...steps, cycleNo: e.target.value })} /></div>
          </div>
          <div className="form-actions">
            <Button disabled={busy} onClick={() => act(() => api.postJson(`/api/cssd/cycles/${sel.cycle_id}/steps`, steps), ar ? "تم تسجيل الخطوات" : "Steps recorded")}>{ar ? "حفظ الخطوات" : "Save steps"}</Button>
          </div>
          <h4>{ar ? "المؤشرات" : "Indicators"}</h4>
          <div className="form-grid">
            <div className="field"><label>{ar ? "كيميائي" : "Chemical"}</label>
              <select value={indicators.chemical} onChange={(e) => setIndicators({ ...indicators, chemical: e.target.value })}>
                <option value="pass">pass</option><option value="fail">fail</option><option value="pending">pending</option>
              </select></div>
            <div className="field"><label>{ar ? "بيولوجي" : "Biological"}</label>
              <select value={indicators.biological} onChange={(e) => setIndicators({ ...indicators, biological: e.target.value })}>
                <option value="">—</option><option value="pass">pass</option><option value="fail">fail</option><option value="pending">pending</option>
              </select></div>
          </div>
          <div className="form-actions">
            <Button disabled={busy} onClick={() => act(() => api.postJson(`/api/cssd/cycles/${sel.cycle_id}/indicators`, indicators), ar ? "تم تسجيل المؤشرات" : "Indicators recorded")}>{ar ? "حفظ المؤشرات" : "Save indicators"}</Button>
          </div>
          <h4>{ar ? "الإفراج للمخزن المعقم" : "Release to sterile storage"}</h4>
          {sel.releasable ? <div className="l-row"><span className="badge ok">{ar ? "جاهز للإفراج" : "Releasable"}</span></div>
            : <div className="l-row"><span className="badge warn">{ar ? "غير جاهز — أكمل السلسلة والمؤشرات" : "Not ready — complete the chain and indicators"}</span></div>}
          <div className="form-grid">
            <div className="field"><label>{ar ? "موقع التخزين" : "Storage location"}</label><input value={release.storageLocation} onChange={(e) => setRelease({ ...release, storageLocation: e.target.value })} /></div>
            <div className="field"><label>{ar ? "تاريخ انتهاء الصلاحية" : "Expiry date"}</label><input type="date" value={release.expiryDate} onChange={(e) => setRelease({ ...release, expiryDate: e.target.value })} /></div>
          </div>
          <div className="form-actions">
            <Button disabled={busy} onClick={() => act(async () => {
              await api.postJson(`/api/cssd/cycles/${sel.cycle_id}/release`, release);
              setSelected(null);
            }, ar ? "تم الإفراج إلى المخزن المعقم" : "Released to sterile storage")}>{ar ? "إفراج" : "Release"}</Button>
          </div>
        </Drawer>
      ) : null}

      {sel && sel.status === "sterile" ? (
        <Drawer title={`${sel.set_code} — ${ar ? "صرف للاستخدام" : "Issue for use"}`} onClose={() => setSelected(null)}>
          <div className="form-grid">
            <div className="field"><label>{ar ? "مرجع الإجراء (إلزامي للتتبع)" : "Procedure reference (required for traceability)"}</label>
              <input value={use.procedureRef} onChange={(e) => setUse({ ...use, procedureRef: e.target.value })} /></div>
            <div className="field"><label>{ar ? "مرجع المريض" : "Patient reference"}</label>
              <input value={use.patientRef} onChange={(e) => setUse({ ...use, patientRef: e.target.value })} /></div>
          </div>
          <div className="form-actions">
            <Button disabled={busy || !use.procedureRef.trim()} onClick={() => act(async () => {
              await api.postJson(`/api/cssd/sets/${sel.set_id}/use`, use);
              setSelected(null);
            }, ar ? "تم الصرف وربطه بالإجراء" : "Issued and linked")}>{ar ? "صرف" : "Issue"}</Button>
          </div>
        </Drawer>
      ) : null}

      {receiveFor ? (
        <Drawer title={`${receiveFor.set_code} — ${ar ? "استلام ملوث" : "Receive contaminated set"}`} onClose={() => setReceiveFor(null)}>
          <div className="form-grid">
            <div className="field"><label>{ar ? "مستلم من" : "Received from"}</label><input value={receive.receivedFrom} onChange={(e) => setReceive({ ...receive, receivedFrom: e.target.value })} /></div>
            <div className="field"><label>{ar ? "فرزها" : "Sorted by"}</label><input value={receive.sortedBy} onChange={(e) => setReceive({ ...receive, sortedBy: e.target.value })} /></div>
          </div>
          <div className="form-actions">
            <Button disabled={busy} onClick={() => act(async () => {
              await api.postJson(`/api/cssd/sets/${receiveFor.set_id}/receive`, receive);
              setReceiveFor(null);
            }, ar ? "بدأت دورة إعادة المعالجة" : "Reprocessing cycle started")}>{ar ? "استلام وبدء الدورة" : "Receive & start cycle"}</Button>
          </div>
        </Drawer>
      ) : null}

      {newSet.open ? (
        <Drawer title={ar ? "تسجيل طقم أدوات" : "Register instrument set"} onClose={() => setNewSet({ ...newSet, open: false })}>
          <div className="form-grid">
            <div className="field"><label>{ar ? "كود الطقم" : "Set code"}</label><input value={newSet.setCode} onChange={(e) => setNewSet({ ...newSet, setCode: e.target.value })} /></div>
            <div className="field"><label>{ar ? "الاسم" : "Name"}</label><input value={newSet.name} onChange={(e) => setNewSet({ ...newSet, name: e.target.value })} /></div>
            <div className="field"><label>{ar ? "يحتوي غرسات" : "Contains implants"}</label><input type="checkbox" checked={newSet.containsImplants} onChange={(e) => setNewSet({ ...newSet, containsImplants: e.target.checked })} /></div>
          </div>
          <div className="form-actions">
            <Button disabled={busy || !newSet.setCode.trim()} onClick={() => act(async () => {
              await api.postJson("/api/cssd/sets", { setCode: newSet.setCode, name: newSet.name, containsImplants: newSet.containsImplants });
              setNewSet({ open: false, setCode: "", name: "", containsImplants: false });
            }, ar ? "تم تسجيل الطقم" : "Set registered")}>{ar ? "تسجيل" : "Register"}</Button>
          </div>
        </Drawer>
      ) : null}

      <Toasts items={toasts} />
    </div>
  );
}
