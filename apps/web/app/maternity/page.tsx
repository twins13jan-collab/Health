"use client";

import { useCallback, useEffect, useState } from "react";
import KpiCard from "@/components/KpiCard";
import { Button, Drawer, PageHead, Toasts } from "@/components/ui";
import { api } from "@/lib/api";
import { useLang } from "@/lib/i18n";

interface Pregnancy { id: string; patientId: string; lmpDate?: string; edd?: string; fetusCount: number; riskLevel?: string; status: string }
interface BoardRow { pregnancy: Pregnancy; patient_name: string; ga_weeks: number | string; ga_days: number | string; overdue: boolean; in_labor: boolean; labor_dilation_cm: number | string; last_bp: string; severe_bp?: boolean; pph_open: boolean; discharge_blocked: boolean; attention_rank: number }
interface BoardResp { board: BoardRow[]; count: number; summary: { in_labor: number; high_risk: number; overdue: number; deliveries_today: number; pph_today: number; unsigned_deliveries: number; discharge_blocked: number; active_caseload: number } }

const RISK_TONE: Record<string, string> = { low: "ok", high: "warn", critical: "danger" };

/** Labour-ward operational board (Phase 4.7, cycle 244): the maternity
 *  caseload as an attention queue — PPH emergencies, women in labour,
 *  critical risk, overdue pregnancies. Risk suggestions from visits are
 *  advisory only; a physician confirms the level. A signed-off delivery
 *  record is immutable, and postpartum discharge stays blocked while
 *  danger signs persist. */
export default function MaternityPage() {
  const { lang, t } = useLang();
  const ar = lang === "ar";
  const [board, setBoard] = useState<BoardResp | null>(null);
  const [selected, setSelected] = useState<BoardRow | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [toasts, setToasts] = useState<{ text: string; err?: boolean }[]>([]);
  const [busy, setBusy] = useState(false);
  const [visit, setVisit] = useState({ bpSystolic: "", bpDiastolic: "", proteinuria: false, bleeding: false, severeSymptoms: false });
  const [risk, setRisk] = useState({ riskLevel: "high", riskFactors: "" });
  const [labor, setLabor] = useState({ cervixDilationCm: "", team: "" });
  const [delivery, setDelivery] = useState({ deliveryType: "vaginal", bloodLossMl: "", newbornCount: "1" });
  const [lastDelivery, setLastDelivery] = useState<{ id: string; pph: boolean } | null>(null);
  const [signer, setSigner] = useState("");
  const [post, setPost] = useState({ bleedingStatus: "normal", bpSystolic: "", bpDiastolic: "", painScore: "", mentalState: "stable" });

  const toast = (text: string, err?: boolean) => { setToasts((x) => [...x, { text, err }]); setTimeout(() => setToasts((x) => x.slice(1)), 4500); };

  const load = useCallback(async () => {
    try {
      const b = await api.getJson<BoardResp>("/api/maternity/board");
      setBoard(b);
      setSelected((sel) => (sel ? b.board.find((r) => r.pregnancy.id === sel.pregnancy.id) ?? null : null));
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

  return (
    <div>
      <PageHead eyebrow={ar ? "التشغيل السريري" : "Clinical operations"} title={t("maternity")}
        sub={ar ? "لوحة جناح الولادة: الحالات كطابور انتباه — طوارئ النزف أولًا ثم المخاض ثم الخطورة الحرجة ثم تجاوز الموعد المتوقع. اقتراح الخطورة من الزيارة استشاري ولا يُطبق إلا باعتماد الطبيب، وسجل الولادة المعتمد لا يُعدَّل."
               : "Labour-ward board: caseload as an attention queue — PPH emergencies, labour, critical risk, overdue. Visit risk suggestions are advisory until a physician confirms; signed-off delivery records are immutable."}
        actions={<Button onClick={load}>{ar ? "تحديث" : "Refresh"}</Button>} />
      {error ? <div className="alert-error">{error}</div> : null}
      <div className="kpi-grid">
        <KpiCard label={ar ? "في مخاض الآن" : "In labour"} value={s?.in_labor ?? "—"} />
        <KpiCard label={ar ? "خطورة عالية/حرجة" : "High/critical risk"} value={s?.high_risk ?? "—"} />
        <KpiCard label={ar ? "تجاوزت الموعد المتوقع" : "Overdue"} value={s?.overdue ?? "—"} />
        <KpiCard label={ar ? "ولادات اليوم" : "Deliveries today"} value={s?.deliveries_today ?? "—"} />
        <KpiCard label={ar ? "نزف اليوم (PPH)" : "PPH today"} value={s?.pph_today ?? "—"} />
        <KpiCard label={ar ? "سجلات بانتظار الاعتماد" : "Awaiting sign-off"} value={s?.unsigned_deliveries ?? "—"} />
      </div>

      {!board ? <div className="empty">{ar ? "جارٍ التحميل…" : "Loading…"}</div>
        : board.board.length === 0 ? <div className="empty">{ar ? "لا حالات أمومة نشطة في منشأتك الآن" : "No active maternity caseload in your facility"}</div>
        : (
          <div className="cards-grid">
            {board.board.map((row) => (
              <section key={row.pregnancy.id} className="card">
                <div className="l-row">
                  <strong>{row.patient_name || row.pregnancy.patientId}</strong>
                  {row.pph_open ? <span className="badge danger">{ar ? "نزف ما بعد الولادة — مسار طوارئ" : "PPH — emergency path"}</span> : null}
                  {row.in_labor ? <span className="badge warn">{ar ? "في مخاض" : "In labour"}{row.labor_dilation_cm !== "" ? ` · ${row.labor_dilation_cm} cm` : ""}</span> : null}
                  {row.pregnancy.riskLevel ? <span className={`badge ${RISK_TONE[row.pregnancy.riskLevel] ?? "info"}`}>{ar ? "خطورة" : "Risk"}: {row.pregnancy.riskLevel}</span> : null}
                  {row.overdue ? <span className="badge danger">{ar ? "تجاوزت الموعد المتوقع" : "Overdue"}</span> : null}
                  {row.discharge_blocked ? <span className="badge danger">{ar ? "الخروج محظور — علامات خطر" : "Discharge blocked — danger signs"}</span> : null}
                </div>
                <div className="l-row small">
                  {row.ga_weeks !== "" ? <span className="badge info">{ar ? "العمر الحملي" : "GA"}: {row.ga_weeks} {ar ? "أسبوع" : "w"} + {row.ga_days}</span> : null}
                  {row.pregnancy.edd ? <span className="badge info">EDD: {row.pregnancy.edd}</span> : null}
                  {row.last_bp ? <span className={`badge ${row.severe_bp ? "danger" : "info"}`}>{ar ? "آخر ضغط" : "Last BP"}: {row.last_bp}</span> : null}
                  {row.pregnancy.fetusCount > 1 ? <span className="badge warn">{ar ? "أجنة" : "Fetuses"}: {row.pregnancy.fetusCount}</span> : null}
                </div>
                <div className="l-actions">
                  <Button onClick={() => { setSelected(row); setLastDelivery(null); }}>{ar ? "إدارة الحالة" : "Manage case"}</Button>
                </div>
              </section>
            ))}
          </div>
        )}

      {sel ? (
        <Drawer title={sel.patient_name || sel.pregnancy.patientId} onClose={() => setSelected(null)}>
          {sel.pregnancy.status === "ongoing" ? (
            <>
              <h4>{ar ? "زيارة متابعة حمل" : "Antenatal visit"}</h4>
              <div className="form-grid">
                <div className="field"><label>{ar ? "الضغط الانقباضي" : "Systolic"}</label><input type="number" value={visit.bpSystolic} onChange={(e) => setVisit({ ...visit, bpSystolic: e.target.value })} /></div>
                <div className="field"><label>{ar ? "الضغط الانبساطي" : "Diastolic"}</label><input type="number" value={visit.bpDiastolic} onChange={(e) => setVisit({ ...visit, bpDiastolic: e.target.value })} /></div>
                <div className="field"><label>{ar ? "بروتين بالبول" : "Proteinuria"}</label><input type="checkbox" checked={visit.proteinuria} onChange={(e) => setVisit({ ...visit, proteinuria: e.target.checked })} /></div>
                <div className="field"><label>{ar ? "نزيف" : "Bleeding"}</label><input type="checkbox" checked={visit.bleeding} onChange={(e) => setVisit({ ...visit, bleeding: e.target.checked })} /></div>
                <div className="field"><label>{ar ? "أعراض شديدة" : "Severe symptoms"}</label><input type="checkbox" checked={visit.severeSymptoms} onChange={(e) => setVisit({ ...visit, severeSymptoms: e.target.checked })} /></div>
                <div className="form-actions"><Button disabled={busy} onClick={() => act(async () => {
                  const r = await api.postJson<{ risk_suggestion: string; escalation_required: boolean }>(`/api/maternity/pregnancies/${sel.pregnancy.id}/visits`, {
                    bpSystolic: visit.bpSystolic ? Number(visit.bpSystolic) : undefined,
                    bpDiastolic: visit.bpDiastolic ? Number(visit.bpDiastolic) : undefined,
                    proteinuria: visit.proteinuria, bleeding: visit.bleeding, severeSymptoms: visit.severeSymptoms,
                  });
                  toast((ar ? "اقتراح الخطورة (استشاري): " : "Advisory risk suggestion: ") + r.risk_suggestion, r.escalation_required);
                }, ar ? "سُجلت الزيارة" : "Visit recorded")}>{ar ? "تسجيل الزيارة" : "Record visit"}</Button></div>
              </div>

              <h4>{ar ? "اعتماد الخطورة (الطبيب فقط)" : "Confirm risk (physician)"}</h4>
              <div className="form-grid">
                <div className="field"><label>{ar ? "المستوى" : "Level"}</label>
                  <select value={risk.riskLevel} onChange={(e) => setRisk({ ...risk, riskLevel: e.target.value })}>
                    <option value="low">low</option><option value="high">high</option><option value="critical">critical</option>
                  </select></div>
                <div className="field"><label>{ar ? "عوامل الخطورة" : "Risk factors"}</label><input value={risk.riskFactors} onChange={(e) => setRisk({ ...risk, riskFactors: e.target.value })} /></div>
                <div className="form-actions"><Button disabled={busy} onClick={() => act(() => api.postJson(`/api/maternity/pregnancies/${sel.pregnancy.id}/risk`, risk), ar ? "اعتُمدت الخطورة" : "Risk confirmed")}>{ar ? "اعتماد" : "Confirm"}</Button></div>
              </div>

              <h4>{ar ? "بدء المخاض" : "Start labour"}</h4>
              <div className="form-grid">
                <div className="field"><label>{ar ? "اتساع عنق الرحم (سم)" : "Cervix dilation (cm)"}</label><input type="number" step="0.5" value={labor.cervixDilationCm} onChange={(e) => setLabor({ ...labor, cervixDilationCm: e.target.value })} /></div>
                <div className="field"><label>{ar ? "الفريق" : "Team"}</label><input value={labor.team} onChange={(e) => setLabor({ ...labor, team: e.target.value })} /></div>
                <div className="form-actions"><Button disabled={busy} onClick={() => act(() => api.postJson(`/api/maternity/pregnancies/${sel.pregnancy.id}/labor`, {
                  cervixDilationCm: labor.cervixDilationCm ? Number(labor.cervixDilationCm) : undefined, team: labor.team || undefined,
                }), ar ? "بدأ المخاض" : "Labour started")}>{ar ? "بدء المخاض" : "Start labour"}</Button></div>
              </div>
            </>
          ) : null}

          {sel.pregnancy.status === "in_labor" || sel.pregnancy.status === "ongoing" ? (
            <>
              <h4>{ar ? "تسجيل الولادة" : "Record delivery"}</h4>
              <div className="form-grid">
                <div className="field"><label>{ar ? "النوع" : "Type"}</label>
                  <select value={delivery.deliveryType} onChange={(e) => setDelivery({ ...delivery, deliveryType: e.target.value })}>
                    <option value="vaginal">{ar ? "مهبلية" : "Vaginal"}</option>
                    <option value="assisted">{ar ? "مساعَدة" : "Assisted"}</option>
                    <option value="cesarean">{ar ? "قيصرية" : "Cesarean"}</option>
                  </select></div>
                <div className="field"><label>{ar ? "فقد الدم (مل)" : "Blood loss (ml)"}</label><input type="number" value={delivery.bloodLossMl} onChange={(e) => setDelivery({ ...delivery, bloodLossMl: e.target.value })} /></div>
                <div className="field"><label>{ar ? "عدد المواليد" : "Newborns"}</label><input type="number" value={delivery.newbornCount} onChange={(e) => setDelivery({ ...delivery, newbornCount: e.target.value })} /></div>
                <div className="form-actions"><Button disabled={busy || !delivery.bloodLossMl} onClick={() => act(async () => {
                  const r = await api.postJson<{ delivery: { id: string }; pph: boolean }>(`/api/maternity/pregnancies/${sel.pregnancy.id}/delivery`, {
                    deliveryType: delivery.deliveryType, bloodLossMl: Number(delivery.bloodLossMl), newbornCount: Number(delivery.newbornCount || 1),
                  });
                  setLastDelivery({ id: r.delivery.id, pph: r.pph });
                  if (r.pph) toast(ar ? "نزف ما بعد الولادة — فُتح مسار الطوارئ" : "PPH — emergency path opened", true);
                }, ar ? "سُجلت الولادة" : "Delivery recorded")}>{ar ? "تسجيل الولادة" : "Record delivery"}</Button></div>
              </div>
              {lastDelivery ? (
                <div className="form-grid">
                  <div className="field"><label>{ar ? "اعتماد سجل الولادة باسم" : "Sign off delivery as"}</label><input value={signer} onChange={(e) => setSigner(e.target.value)} /></div>
                  <div className="form-actions"><Button disabled={busy || !signer} onClick={() => act(() => api.postJson(`/api/maternity/deliveries/${lastDelivery.id}/sign-off`, { signedBy: signer }), ar ? "اعتُمد السجل — لا يُعدَّل بعد الاعتماد" : "Record signed off — immutable now")}>{ar ? "اعتماد نهائي" : "Sign off"}</Button></div>
                </div>
              ) : null}
            </>
          ) : null}

          {sel.pregnancy.status === "delivered" || sel.pregnancy.status === "postpartum" ? (
            <>
              <h4>{ar ? "تقييم النفاس والخروج" : "Postpartum assessment"}</h4>
              <div className="form-grid">
                <div className="field"><label>{ar ? "حالة النزيف" : "Bleeding"}</label>
                  <select value={post.bleedingStatus} onChange={(e) => setPost({ ...post, bleedingStatus: e.target.value })}>
                    <option value="normal">normal</option><option value="heavy">heavy</option><option value="none">none</option>
                  </select></div>
                <div className="field"><label>{ar ? "الضغط الانقباضي" : "Systolic"}</label><input type="number" value={post.bpSystolic} onChange={(e) => setPost({ ...post, bpSystolic: e.target.value })} /></div>
                <div className="field"><label>{ar ? "الضغط الانبساطي" : "Diastolic"}</label><input type="number" value={post.bpDiastolic} onChange={(e) => setPost({ ...post, bpDiastolic: e.target.value })} /></div>
                <div className="field"><label>{ar ? "الألم (0-10)" : "Pain (0-10)"}</label><input type="number" value={post.painScore} onChange={(e) => setPost({ ...post, painScore: e.target.value })} /></div>
                <div className="field"><label>{ar ? "الحالة النفسية" : "Mental state"}</label><input value={post.mentalState} onChange={(e) => setPost({ ...post, mentalState: e.target.value })} /></div>
                <div className="form-actions"><Button disabled={busy} onClick={() => act(async () => {
                  const r = await api.postJson<{ discharge_blocked: boolean }>(`/api/maternity/pregnancies/${sel.pregnancy.id}/postpartum`, {
                    bleedingStatus: post.bleedingStatus,
                    bpSystolic: post.bpSystolic ? Number(post.bpSystolic) : undefined,
                    bpDiastolic: post.bpDiastolic ? Number(post.bpDiastolic) : undefined,
                    painScore: post.painScore ? Number(post.painScore) : undefined,
                    mentalState: post.mentalState,
                  });
                  if (r.discharge_blocked) toast(ar ? "الخروج محظور — علامات خطر تستلزم مراجعة الطبيب" : "Discharge blocked — danger signs need physician review", true);
                }, ar ? "سُجل تقييم النفاس" : "Postpartum assessed")}>{ar ? "تسجيل التقييم" : "Record assessment"}</Button></div>
              </div>
            </>
          ) : null}
        </Drawer>
      ) : null}
      <Toasts items={toasts} />
    </div>
  );
}
