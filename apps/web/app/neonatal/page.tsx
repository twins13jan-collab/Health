"use client";

import { useCallback, useEffect, useState } from "react";
import KpiCard from "@/components/KpiCard";
import { Button, Drawer, PageHead, Toasts } from "@/components/ui";
import { api } from "@/lib/api";
import { useLang } from "@/lib/i18n";

interface NicuStay { id: string; newbornId: string; reason: string; respiratorySupport?: string; feedingType?: string; handoverNotes?: string; startedAt: string; status: string }
interface BoardRow { stay: NicuStay; birth_weight_g: number | string; weight_class: string; apgar_5min: number | string; apgar_band: string; screenings_total: number; screenings_pending: number; screenings_abnormal: number; referrals_open: number; recollect_required: number }
interface BoardResp { board: BoardRow[]; count: number; summary: { active_stays: number; low_birth_weight: number; open_referrals: number; pending_screenings: number; abnormal_screenings: number } }

const WEIGHT_AR: Record<string, string> = { normal: "وزن طبيعي", low: "وزن منخفض", very_low: "وزن منخفض جدًا", extremely_low: "وزن منخفض للغاية" };

/** NICU operational board (Phase 4.6, cycle 245): active incubator
 *  stays with birth-weight class and Apgar band from the neonatal rules,
 *  and the screening pipeline state — abnormal results keep an open
 *  referral until a documented outcome closes it, and invalid samples
 *  are recollected, never read as normal. Discharge/transfer requires a
 *  documented handover (enforced by the server). */
export default function NeonatalPage() {
  const { lang, t } = useLang();
  const ar = lang === "ar";
  const [board, setBoard] = useState<BoardResp | null>(null);
  const [selected, setSelected] = useState<BoardRow | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [toasts, setToasts] = useState<{ text: string; err?: boolean }[]>([]);
  const [busy, setBusy] = useState(false);
  const [endStatus, setEndStatus] = useState("discharged");
  const [handover, setHandover] = useState("");
  const [screenType, setScreenType] = useState("hearing");

  const toast = (text: string, err?: boolean) => { setToasts((x) => [...x, { text, err }]); setTimeout(() => setToasts((x) => x.slice(1)), 4000); };

  const load = useCallback(async () => {
    try {
      const b = await api.getJson<BoardResp>("/api/neonatal/board");
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
      <PageHead eyebrow={ar ? "التشغيل السريري" : "Clinical operations"} title={t("neonatal")}
        sub={ar ? "لوحة الحضانات: الإقامات النشطة بوزن الولادة المصنّف وأبغار، وحالة خط الفحص المبكر — الإحالة المفتوحة لا تُغلق إلا بنتيجة موثقة، والعينة غير الصالحة تُعاد ولا تُعد طبيعية."
               : "NICU board: active stays with classified birth weight and Apgar, and the screening pipeline — open referrals close only with a documented outcome; invalid samples are recollected, never normal."}
        actions={<Button onClick={load}>{ar ? "تحديث" : "Refresh"}</Button>} />
      {error ? <div className="alert-error">{error}</div> : null}
      <div className="kpi-grid">
        <KpiCard label={ar ? "مواليد بالحضانة" : "In NICU"} value={s?.active_stays ?? "—"} />
        <KpiCard label={ar ? "وزن ولادة منخفض" : "Low birth weight"} value={s?.low_birth_weight ?? "—"} />
        <KpiCard label={ar ? "إحالات فحص مفتوحة" : "Open referrals"} value={s?.open_referrals ?? "—"} />
        <KpiCard label={ar ? "فحوص معلقة" : "Pending screenings"} value={s?.pending_screenings ?? "—"} />
        <KpiCard label={ar ? "نتائج غير طبيعية" : "Abnormal results"} value={s?.abnormal_screenings ?? "—"} />
      </div>

      {!board ? <div className="empty">{ar ? "جارٍ التحميل…" : "Loading…"}</div>
        : board.board.length === 0 ? <div className="empty">{ar ? "لا إقامات حضانة نشطة في منشأتك الآن" : "No active NICU stays in your facility"}</div>
        : (
          <div className="cards-grid">
            {board.board.map((row) => (
              <section key={row.stay.id} className="card">
                <div className="l-row">
                  <strong>{row.stay.reason}</strong>
                  {row.weight_class ? <span className={`badge ${row.weight_class === "normal" ? "ok" : "warn"}`}>{ar ? (WEIGHT_AR[row.weight_class] ?? row.weight_class) : row.weight_class}{row.birth_weight_g ? ` · ${row.birth_weight_g}g` : ""}</span> : null}
                  {row.apgar_band ? <span className={`badge ${row.apgar_band === "low_critical" ? "danger" : "ok"}`}>Apgar {row.apgar_5min} · {row.apgar_band}</span> : null}
                </div>
                <div className="l-row small">
                  {row.stay.respiratorySupport ? <span className="badge info">{ar ? "دعم تنفسي" : "Support"}: {row.stay.respiratorySupport}</span> : null}
                  {row.stay.feedingType ? <span className="badge info">{ar ? "تغذية" : "Feeding"}: {row.stay.feedingType}</span> : null}
                </div>
                <div className="l-row">
                  {row.screenings_pending > 0 ? <span className="badge warn">{ar ? "فحص معلق" : "Pending"} · {row.screenings_pending}</span> : null}
                  {row.screenings_abnormal > 0 ? <span className="badge danger">{ar ? "نتيجة غير طبيعية" : "Abnormal"} · {row.screenings_abnormal}</span> : null}
                  {row.referrals_open > 0 ? <span className="badge danger">{ar ? "إحالة مفتوحة" : "Referral open"} · {row.referrals_open}</span> : null}
                  {row.recollect_required > 0 ? <span className="badge warn">{ar ? "عينة تُعاد" : "Recollect"} · {row.recollect_required}</span> : null}
                  {row.screenings_total === 0 ? <span className="small muted">{ar ? "لا فحوص بعد" : "No screenings yet"}</span> : null}
                </div>
                <div className="l-actions">
                  <Button onClick={() => setSelected(row)}>{ar ? "إدارة الإقامة" : "Manage stay"}</Button>
                </div>
              </section>
            ))}
          </div>
        )}

      {selected ? (
        <Drawer title={selected.stay.reason} onClose={() => setSelected(null)}>
          <h4>{ar ? "طلب فحص مبكر" : "Order screening"}</h4>
          <div className="form-grid">
            <div className="field"><label>{ar ? "نوع الفحص" : "Screening type"}</label>
              <select value={screenType} onChange={(e) => setScreenType(e.target.value)}>
                <option value="hearing">{ar ? "سمع" : "Hearing"}</option>
                <option value="metabolic">{ar ? "أيضي (PKU/غدة)" : "Metabolic"}</option>
                <option value="cchd">{ar ? "قلب خلقي CCHD" : "CCHD"}</option>
                <option value="bilirubin">{ar ? "صفراء" : "Bilirubin"}</option>
              </select></div>
            <div className="form-actions"><Button disabled={busy} onClick={() => act(() => api.postJson(`/api/neonatal/newborns/${selected.stay.newbornId}/screenings`, { screeningType: screenType }), ar ? "طُلب الفحص" : "Screening ordered")}>{ar ? "طلب الفحص" : "Order"}</Button></div>
          </div>

          <h4>{ar ? "إنهاء الإقامة" : "End stay"}</h4>
          <div className="small muted">{ar ? "الخروج أو التحويل يتطلب تسليمًا سريريًا موثقًا — يُرفض الإنهاء بدونه." : "Discharge or transfer requires a documented clinical handover — ending without one is rejected."}</div>
          <div className="form-grid">
            <div className="field"><label>{ar ? "الحالة" : "Outcome"}</label>
              <select value={endStatus} onChange={(e) => setEndStatus(e.target.value)}>
                <option value="discharged">{ar ? "خروج" : "Discharged"}</option>
                <option value="transferred">{ar ? "تحويل" : "Transferred"}</option>
                <option value="deceased">{ar ? "وفاة" : "Deceased"}</option>
              </select></div>
            <div className="field"><label>{ar ? "ملاحظات التسليم" : "Handover notes"}</label><input value={handover} onChange={(e) => setHandover(e.target.value)} /></div>
            <div className="form-actions"><Button disabled={busy} onClick={() => act(async () => {
              await api.postJson(`/api/neonatal/nicu/${selected.stay.id}/end`, { status: endStatus, handoverNotes: handover });
              setSelected(null);
            }, ar ? "أُنهيت الإقامة" : "Stay ended")}>{ar ? "إنهاء" : "End stay"}</Button></div>
          </div>
        </Drawer>
      ) : null}
      <Toasts items={toasts} />
    </div>
  );
}
