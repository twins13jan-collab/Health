"use client";

import { useCallback, useEffect, useState } from "react";
import KpiCard from "@/components/KpiCard";
import { Button, Drawer, PageHead, Toasts } from "@/components/ui";
import { api } from "@/lib/api";
import { useLang } from "@/lib/i18n";

interface RecRow { encounter_id: string; patient_id: string; patient_name: string; type: string; chief_complaint?: string; days_waiting: number; deficiencies: string[]; ready: boolean; overdue: boolean; summary_status?: string; summary_text?: string; primary_icd10?: string; secondary_icd10?: string }
interface BoardResp { board: RecRow[]; count: number; summary: { awaiting_filing: number; ready_to_file: number; deficient: number; overdue: number; missing_notes: number; missing_summary: number; missing_coding: number; filed_today: number } }

const DEF_LABEL: Record<string, { ar: string; en: string }> = {
  clinical_note_missing: { ar: "ملاحظة سريرية موقعة مفقودة", en: "Signed clinical note missing" },
  discharge_summary_missing: { ar: "ملخص إغلاق مفقود", en: "Closing summary missing" },
  discharge_summary_not_final: { ar: "ملخص الإغلاق مسودة غير نهائية", en: "Closing summary still draft" },
  icd10_coding_missing: { ar: "ترميز ICD-10 مفقود", en: "ICD-10 coding missing" },
};

/** Medical records & coding worklist (Phase 4.9): completed encounters
 *  awaiting filing. A file closes only when it holds a signed note, a
 *  final summary and ICD-10 coding validated against the national
 *  reference set — deficiencies stay named on the board, and files
 *  older than the 14-day filing SLA are flagged overdue. */
export default function MedicalRecordsPage() {
  const { lang, t } = useLang();
  const ar = lang === "ar";
  const [board, setBoard] = useState<BoardResp | null>(null);
  const [selected, setSelected] = useState<RecRow | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [toasts, setToasts] = useState<{ text: string; err?: boolean }[]>([]);
  const [busy, setBusy] = useState(false);
  const [summaryText, setSummaryText] = useState("");
  const [coding, setCoding] = useState({ primaryIcd10: "", secondaryIcd10: "" });

  const toast = (text: string, err?: boolean) => { setToasts((x) => [...x, { text, err }]); setTimeout(() => setToasts((x) => x.slice(1)), 4500); };

  const load = useCallback(async () => {
    try {
      const b = await api.getJson<BoardResp>("/api/medical-records/board");
      setBoard(b);
      setSelected((sel) => (sel ? b.board.find((r) => r.encounter_id === sel.encounter_id) ?? null : null));
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
  const defLabel = (d: string) => (ar ? DEF_LABEL[d]?.ar ?? d : DEF_LABEL[d]?.en ?? d);

  return (
    <div>
      <PageHead eyebrow={ar ? "التشغيل السريري" : "Clinical operations"} title={t("medicalRecords")}
        sub={ar ? "قائمة عمل السجلات: اللقاءات المكتملة بانتظار إقفال ملفاتها. لا يُقفل ملف إلا بملاحظة سريرية موقعة وملخص إغلاق نهائي وترميز ICD-10 معتمد من المجموعة المرجعية الوطنية — والنواقص تبقى مسماة على اللوحة، والملف المتأخر عن مهلة 14 يومًا يُعلَّم صراحة."
               : "The records worklist: completed encounters awaiting filing. A file closes only with a signed note, a final summary and ICD-10 coding validated against the national reference set — deficiencies stay named, and files past the 14-day SLA are flagged."}
        actions={<Button onClick={load}>{ar ? "تحديث" : "Refresh"}</Button>} />
      {error ? <div className="alert-error">{error}</div> : null}
      <div className="kpi-grid">
        <KpiCard label={ar ? "بانتظار الإقفال" : "Awaiting filing"} value={s?.awaiting_filing ?? "—"} />
        <KpiCard label={ar ? "جاهز للإقفال" : "Ready to file"} value={s?.ready_to_file ?? "—"} />
        <KpiCard label={ar ? "ملفات ناقصة" : "Deficient files"} value={s?.deficient ?? "—"} />
        <KpiCard label={ar ? "متأخرة عن المهلة" : "Overdue"} value={s?.overdue ?? "—"} />
        <KpiCard label={ar ? "ترميز مفقود" : "Coding missing"} value={s?.missing_coding ?? "—"} />
        <KpiCard label={ar ? "أُقفل اليوم" : "Filed today"} value={s?.filed_today ?? "—"} />
      </div>

      {!board ? <div className="empty">{ar ? "جارٍ التحميل…" : "Loading…"}</div>
        : board.board.length === 0 ? <div className="empty">{ar ? "لا ملفات بانتظار الإقفال في منشأتك" : "No files awaiting filing in your facility"}</div>
        : (
          <div className="cards-grid">
            {board.board.map((row) => (
              <section key={row.encounter_id} className="card">
                <div className="l-row">
                  <strong>{row.patient_name || row.patient_id}</strong>
                  {row.ready ? <span className="badge ok">{ar ? "جاهز للإقفال" : "Ready to file"}</span> : null}
                  {row.overdue ? <span className="badge danger">{ar ? "متأخر عن مهلة الإقفال" : "Past filing SLA"}</span> : null}
                  <span className="badge info">{row.type}</span>
                  <span className="badge warn">{ar ? "منذ الاكتمال" : "completed"} {row.days_waiting} {ar ? "يوم" : "d"}</span>
                </div>
                <div className="l-row">
                  {row.deficiencies.map((d) => <span key={d} className="badge danger">{defLabel(d)}</span>)}
                  {row.primary_icd10 ? <span className="badge ok">ICD-10: {row.primary_icd10}</span> : null}
                  {row.summary_status ? <span className={`badge ${row.summary_status === "final" ? "ok" : "warn"}`}>{ar ? "الملخص" : "Summary"}: {row.summary_status}</span> : null}
                </div>
                {row.chief_complaint ? <div className="l-row small">{row.chief_complaint}</div> : null}
                <div className="l-actions">
                  <Button onClick={() => { setSelected(row); setSummaryText(row.summary_text ?? ""); setCoding({ primaryIcd10: row.primary_icd10 ?? "", secondaryIcd10: row.secondary_icd10 ?? "" }); }}>
                    {ar ? "استكمال الملف" : "Complete file"}
                  </Button>
                </div>
              </section>
            ))}
          </div>
        )}

      {sel ? (
        <Drawer title={sel.patient_name || sel.patient_id} onClose={() => setSelected(null)}>
          <h4>{ar ? "ملخص إغلاق اللقاء" : "Encounter closing summary"}</h4>
          <div className="form-grid">
            <div className="field"><label>{ar ? "نص الملخص" : "Summary text"}</label>
              <textarea rows={5} value={summaryText} onChange={(e) => setSummaryText(e.target.value)} disabled={sel.summary_status === "final"} /></div>
          </div>
          <div className="form-actions">
            {sel.summary_status !== "final" ? (
              <Button disabled={busy || !summaryText.trim()} onClick={() => act(() => api.postJson(`/api/medical-records/${sel.encounter_id}/summary`, { text: summaryText }),
                ar ? "تم حفظ الملخص (مسودة)" : "Summary saved (draft)")}>{ar ? "حفظ المسودة" : "Save draft"}</Button>
            ) : null}
            {sel.summary_status === "draft" ? (
              <Button disabled={busy} onClick={() => act(() => api.postJson(`/api/medical-records/${sel.encounter_id}/summary/finalize`, {}),
                ar ? "تم اعتماد الملخص نهائيًا" : "Summary finalized")}>{ar ? "اعتماد نهائي" : "Finalize"}</Button>
            ) : null}
          </div>

          <h4>{ar ? "الترميز ICD-10" : "ICD-10 coding"}</h4>
          <div className="form-grid">
            <div className="field"><label>{ar ? "الكود الرئيسي (يُتحقق منه في المجموعة المرجعية)" : "Primary code (validated against the reference set)"}</label>
              <input value={coding.primaryIcd10} onChange={(e) => setCoding({ ...coding, primaryIcd10: e.target.value })} placeholder="J18.9" /></div>
            <div className="field"><label>{ar ? "أكواد ثانوية (مفصولة بفواصل)" : "Secondary codes (comma-separated)"}</label>
              <input value={coding.secondaryIcd10} onChange={(e) => setCoding({ ...coding, secondaryIcd10: e.target.value })} /></div>
          </div>
          <div className="form-actions">
            <Button disabled={busy || !coding.primaryIcd10.trim()} onClick={() => act(() => api.postJson(`/api/medical-records/${sel.encounter_id}/coding`, coding),
              ar ? "تم حفظ الترميز" : "Coding saved")}>{ar ? "حفظ الترميز" : "Save coding"}</Button>
          </div>

          <h4>{ar ? "إقفال الملف" : "File the record"}</h4>
          {sel.deficiencies.length > 0 ? (
            <div className="l-row">{sel.deficiencies.map((d) => <span key={d} className="badge danger">{defLabel(d)}</span>)}</div>
          ) : null}
          <div className="form-actions">
            <Button disabled={busy} onClick={() => act(async () => {
              await api.postJson(`/api/medical-records/${sel.encounter_id}/file`, {});
              setSelected(null);
            }, ar ? "تم إقفال الملف وأرشفته" : "File closed")}>{ar ? "إقفال وأرشفة" : "File & archive"}</Button>
          </div>
        </Drawer>
      ) : null}

      <Toasts items={toasts} />
    </div>
  );
}
